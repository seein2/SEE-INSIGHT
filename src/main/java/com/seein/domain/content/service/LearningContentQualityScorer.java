package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 학습 콘텐츠 후보 품질 점수 계산
 * Brave에서 가져온 여러 후보 중 "학습 카드로 써도 되는 후보"를 고르는 필터 역할을 한다.
 *
 * 점수 기준:
 * - 언어 일치: 최대 25점
 * - 출처 신뢰도: 최대 20점
 * - snippet 풍부함: 최대 20점
 * - 학습 스타일 적합도: 최대 20점
 * - 최신성: 최대 15점
 * - 같은 도메인 반복 패널티: 최대 30점 차감
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningContentQualityScorer {

    /*
     * 이 점수 이상이면 학습 콘텐츠로 사용할 수 있다고 판단한다.
     * 이 점수보다 낮으면 rejectReason이 "score_below_threshold"로 들어간다.
     */
    public static final int MIN_ACCEPTED_SCORE = 60;

    private static final int EXACT_LANGUAGE_SCORE = 25;
    private static final int INFERRED_LANGUAGE_SCORE = 18;

    private final ContentSourceQualityProperties sourceQualityProperties;
    private final LearningContentTextSanitizer textSanitizer;

    /**
     * 후보 목록 전체를 점수화하고, 높은 점수 순서로 정렬한다.
     *
     * 이 메서드의 결과는 BraveSearchLearningContentGenerator에서
     * accepted() == true인 첫 번째 후보를 선택하는 데 사용된다.
     */
    public List<ScoredCandidate> score(List<LearningContentCandidate> candidates, StudyLanguage studyLanguage, LearningStyle learningStyle) {

        // 원점수 기준으로 먼저 정렬한 뒤 도메인 중복 감점을 적용해야 검색 결과 입력 순서가 점수를 왜곡하지 않는다.
        List<ScoreDraft> scoreDrafts = candidates.stream()
                .map(candidate -> scoreCandidate(candidate, studyLanguage, learningStyle))
                .sorted(Comparator.comparingInt(ScoreDraft::score).reversed())
                .toList();

        // hostCounts는 도메인별 등장 횟수를 세고, duplicatePenalty()에서 감점에 사용한다.
        Map<String, Integer> hostCounts = new HashMap<>();
        List<ScoredCandidate> scoredCandidates = scoreDrafts.stream()
                .map(scoreDraft -> applyDuplicatePenalty(scoreDraft, hostCounts))
                .sorted(Comparator.comparingInt(ScoredCandidate::score).reversed())
                .toList();

        scoredCandidates.forEach(scored -> log.debug(
                "학습 콘텐츠 후보 점수 - score={}, rejectReason={}, url={}, host={}",
                scored.score(),
                scored.rejectReason(),
                scored.candidate().url(),
                scored.candidate().sourceHost()
        ));
        return scoredCandidates;
    }

    /*
     * 후보 하나를 실제로 점수화한다.
     *
     * 처리 순서:
     * 1. 출처 도메인 정규화
     * 2. 차단 도메인 즉시 reject
     * 3. snippet 정제 후 사용할 수 없는 원문이면 reject
     * 4. 언어 불일치 후보 reject
     * 5. 언어/출처/snippet/스타일/최신성 원점수 합산
     * 6. 같은 도메인 반복 패널티와 기준 점수 판정은 applyDuplicatePenalty()에서 처리
     */
    private ScoreDraft scoreCandidate(LearningContentCandidate candidate, StudyLanguage studyLanguage, LearningStyle learningStyle) {
        // sourceHost가 없으면 URL에서 host를 뽑아낸다. 예: https://www.bbc.com/news -> bbc.com
        String host = normalizeHost(candidate.sourceHost());
        if (!StringUtils.hasText(host)) {
            host = normalizeHost(candidate.url());
        }

        // SNS/동영상/이미지 중심 도메인은 학습용 원문으로 부적합하다고 보고 바로 탈락시킨다. ex) x.com, youtube.com, instagram.com 등
        if (sourceQualityProperties.isBlocked(host)) {
            return new ScoreDraft(candidate, host, 0, "blocked_domain");
        }

        // Brave description/extra_snippets/LLM Context snippet을 정제해서 실제 sourceText 후보를 만든다. (HTML 태그, entity, 너무 짧은 문장, 깨진 snippet은 여기서 걸러진다.)
        String sourceText = textSanitizer.sanitizeSourceText(candidate.snippets());
        if (!textSanitizer.isUsableSourceText(sourceText)) {
            return new ScoreDraft(candidate, host, 0, "unusable_snippet");
        }

        int languageScore = languageScore(candidate, studyLanguage, sourceText);
        if (languageScore == 0) {
            return new ScoreDraft(candidate, host, 0, "language_mismatch");
        }

        // "쓸 수는 있는 후보"에 점수를 더하는 단계다. 총점은 100점 만점에 가깝고, 이후 중복 도메인 패널티가 차감된다.
        int score = languageScore;
        score += sourceScore(host);
        score += snippetRichnessScore(sourceText);
        score += styleFitScore(candidate, learningStyle, sourceText);
        score += freshnessScore(candidate, learningStyle);

        return new ScoreDraft(candidate, host, score, null);
    }

    private ScoredCandidate applyDuplicatePenalty(ScoreDraft scoreDraft, Map<String, Integer> hostCounts) {
        if (scoreDraft.rejectReason() != null) {
            return new ScoredCandidate(scoreDraft.candidate(), scoreDraft.score(), scoreDraft.rejectReason());
        }

        int duplicatePenalty = duplicatePenalty(scoreDraft.host(), hostCounts);
        int score = Math.max(0, scoreDraft.score() - duplicatePenalty);
        String rejectReason = score >= MIN_ACCEPTED_SCORE ? null : "score_below_threshold";
        return new ScoredCandidate(scoreDraft.candidate(), score, rejectReason);
    }

    /*
     * 학습하려는 언어와 후보 콘텐츠의 언어가 맞는지 확인한다.
     *
     * 1순위: Brave 응답의 language 필드가 명확하면 그 값을 사용한다.
     * 2순위: language 필드가 없거나 unknown이면 sourceText 문자 비율로 대략 판정한다.
     *
     * 예:
     * - 영어: 라틴 단어가 충분하고 전체 문자 중 라틴 비중이 높은지
     * - 일본어: 히라가나/가타카나가 충분히 포함되어 있는지
     * - 중국어: CJK 한자가 충분하고 일본어 kana가 섞이지 않았는지
     */
    private int languageScore(LearningContentCandidate candidate, StudyLanguage studyLanguage, String sourceText) {
        String language = candidate.language();
        if (StringUtils.hasText(language) && !isUnknownLanguage(language)) {
            return languageMatches(language, studyLanguage) ? EXACT_LANGUAGE_SCORE : 0;
        }

        return inferredLanguageMatches(sourceText, studyLanguage) ? INFERRED_LANGUAGE_SCORE : 0;
    }

    /*
     * 출처 도메인의 신뢰도를 점수화한다.
     *
     * - trustedDomains에 있으면 20점
     * - 모르는 도메인이지만 차단 도메인은 아니면 12점
     * - host 자체가 없으면 판단 근거가 부족하므로 8점
     */
    private int sourceScore(String host) {
        if (!StringUtils.hasText(host)) {
            return 8;
        }
        return sourceQualityProperties.isTrusted(host) ? 20 : 12;
    }

    /*
     * 실제 학습 카드에 들어갈 sourceText가 학습 콘텐츠를 만들 만큼 충분한지 본다.
     * raw snippet 전체가 아니라 sanitizer가 선택한 원문 기준으로 평가한다.
     */
    private int snippetRichnessScore(String sourceText) {
        int sourceTextLength = sourceText.length();
        if (sourceTextLength >= 260) {
            return 20;
        }
        if (sourceTextLength >= 160) {
            return 16;
        }
        if (sourceTextLength >= 80) {
            return 10;
        }
        return 4;
    }

    /*
     * 후보가 사용자가 선택한 학습 스타일과 잘 맞는지 평가한다.
     *
     * NEWS_READING:
     * - news/search 결과이거나 article/news 성격이면 높은 점수
     *
     * TODAYS_EXPRESSION:
     * - expression, idiom, phrase, 表現, 表达 같은 표현 학습 키워드가 있으면 높은 점수
     *
     * BALANCED:
     * - article/learning/phrase 성격이 있거나 sourceText가 충분히 길면 높은 점수
     */
    private int styleFitScore(LearningContentCandidate candidate, LearningStyle learningStyle, String sourceText) {
        String haystack = (candidate.title() + " " + sourceText + " " + candidate.contentType()).toLowerCase(Locale.ROOT);
        return switch (learningStyle) {
            case NEWS_READING -> candidate.sourceType() == ContentSourceType.NEWS
                    || haystack.contains("news")
                    || haystack.contains("article") ? 20 : 10;
            case TODAYS_EXPRESSION -> haystack.contains("expression")
                    || haystack.contains("idiom")
                    || haystack.contains("phrase")
                    || haystack.contains("表現")
                    || haystack.contains("表达") ? 20 : 12;
            case BALANCED -> haystack.contains("article")
                    || haystack.contains("learning")
                    || haystack.contains("phrase")
                    || sourceText.length() >= 80 ? 18 : 10;
            case DAILY_CONVERSATION -> haystack.contains("conversation") || haystack.contains("会話") || haystack.contains("会话") ? 18 : 10;
        };
    }

    /*
     * 최신성 점수다.
     *
     * TODAYS_EXPRESSION은 오래 쓰이는 evergreen 표현이 더 중요하므로 freshness가 없어도 만점 처리한다.
     * 뉴스/균형 학습은 breaking이거나 pageAge가 있으면 최신성 근거가 있다고 보고 높은 점수를 준다.
     */
    private int freshnessScore(LearningContentCandidate candidate, LearningStyle learningStyle) {
        if (learningStyle == LearningStyle.TODAYS_EXPRESSION) {
            return 15;
        }

        if (Boolean.TRUE.equals(candidate.breaking())) {
            return 15;
        }

        return StringUtils.hasText(candidate.pageAge()) ? 14 : 8;
    }

    /*
     * 같은 도메인 후보가 반복되면 감점한다.
     *
     * 이유:
     * - 검색 결과 상위가 같은 사이트로만 채워지면 콘텐츠 다양성이 떨어진다.
     * - 첫 번째 후보는 감점하지 않고, 두 번째부터 15점씩 감점한다.
     * - 최대 감점은 30점이다.
     */
    private int duplicatePenalty(String host, Map<String, Integer> hostCounts) {
        if (!StringUtils.hasText(host)) {
            return 0;
        }

        int count = hostCounts.getOrDefault(host, 0);
        hostCounts.put(host, count + 1);
        return Math.min(30, count * 15);
    }

    /*
     * Brave가 준 명시 언어 값이 사용자의 학습 언어와 일치하는지 확인한다.
     * en-US, ja_JP처럼 하위 태그가 붙은 값도 같은 언어로 본다.
     */
    private boolean languageMatches(String language, StudyLanguage studyLanguage) {
        String normalizedLanguage = normalizeLanguage(language);
        return switch (studyLanguage) {
            case ENGLISH -> normalizedLanguage.startsWith("en")
                    || normalizedLanguage.equals("english");
            case JAPANESE -> normalizedLanguage.startsWith("ja")
                    || normalizedLanguage.equals("jp")
                    || normalizedLanguage.equals("japanese");
            case CHINESE -> normalizedLanguage.startsWith("zh")
                    || normalizedLanguage.startsWith("cn")
                    || normalizedLanguage.equals("chinese");
        };
    }

    /*
     * Brave 언어 값이 불명확해서 sourceText 기반 추정으로 넘어가도 되는지 확인한다.
     * unknown/und/mul 같은 값은 언어 불일치로 바로 탈락시키지 않는다.
     */
    private boolean isUnknownLanguage(String language) {
        String normalizedLanguage = normalizeLanguage(language);
        return normalizedLanguage.equals("unknown")
                || normalizedLanguage.equals("und")
                || normalizedLanguage.equals("mul")
                || normalizedLanguage.equals("none");
    }

    /*
     * 언어 코드 비교가 흔들리지 않도록 대소문자와 구분자 형식을 맞춘다.
     */
    private String normalizeLanguage(String language) {
        return language.toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .trim();
    }

    /*
     * 명시 언어가 없을 때 sourceText의 문자 비율로 대상 언어 여부를 추정한다.
     * 단일 문자 포함 여부만 보면 혼합 언어 문서가 잘못 통과할 수 있어 비율 조건을 함께 본다.
     */
    private boolean inferredLanguageMatches(String sourceText, StudyLanguage studyLanguage) {
        int letterCount = countLetters(sourceText);
        if (letterCount == 0) {
            return false;
        }

        int latinLetterCount = countLatinLetters(sourceText);
        int kanaCount = countKana(sourceText);
        int cjkCount = countCjkIdeographs(sourceText);

        return switch (studyLanguage) {
            case ENGLISH -> countLatinWords(sourceText) >= 4
                    && latinLetterCount * 100 >= letterCount * 60;
            case JAPANESE -> kanaCount >= 3
                    && kanaCount * 100 >= letterCount * 10;
            case CHINESE -> cjkCount >= 8
                    && cjkCount * 100 >= letterCount * 50
                    && kanaCount == 0;
        };
    }

    /*
     * 언어별 문자 비율 계산에 사용할 전체 문자 수를 센다.
     */
    private int countLetters(String text) {
        int count = 0;
        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            if (Character.isLetter(codePoint)) {
                count++;
            }
            index += Character.charCount(codePoint);
        }
        return count;
    }

    /*
     * 영어 비율 계산에 사용할 라틴 알파벳 수를 센다.
     */
    private int countLatinLetters(String text) {
        int count = 0;
        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            if ((codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z')) {
                count++;
            }
            index += Character.charCount(codePoint);
        }
        return count;
    }

    /*
     * 일본어 판정에 사용할 히라가나/가타카나 문자 수를 센다.
     */
    private int countKana(String text) {
        int count = 0;
        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            if ((codePoint >= 0x3040 && codePoint <= 0x30FF)
                    || (codePoint >= 0x31F0 && codePoint <= 0x31FF)) {
                count++;
            }
            index += Character.charCount(codePoint);
        }
        return count;
    }

    /*
     * 중국어 판정에 사용할 CJK 통합 한자 수를 센다.
     */
    private int countCjkIdeographs(String text) {
        int count = 0;
        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            if (codePoint >= 0x4E00 && codePoint <= 0x9FFF) {
                count++;
            }
            index += Character.charCount(codePoint);
        }
        return count;
    }

    /*
     * 영어 판정에서 짧은 약어가 아닌 실제 단어가 충분한지 확인하기 위해 라틴 단어 수를 센다.
     */
    private int countLatinWords(String text) {
        String[] words = text.split("[^A-Za-z]+");
        int count = 0;
        for (String word : words) {
            if (word.length() >= 3) {
                count++;
            }
        }
        return count;
    }

    /*
     * URL 또는 host 문자열을 비교하기 쉬운 형태로 바꾼다.
     *
     * 예:
     * - https://www.bbc.com/news/world -> bbc.com
     * - www.reuters.com -> reuters.com
     */
    private String normalizeHost(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "")
                .split("/")[0]
                .trim();
    }

    /*
     * 점수화 결과를 담는 record.
     *
     * rejectReason이 null이면 통과 후보이고,
     * 값이 있으면 어떤 이유로 탈락했는지 나타낸다.
     */
    public record ScoredCandidate(LearningContentCandidate candidate, int score, String rejectReason) {

        public boolean accepted() {
            return rejectReason == null;
        }
    }

    private record ScoreDraft(LearningContentCandidate candidate, String host, int score, String rejectReason) {
    }
}
