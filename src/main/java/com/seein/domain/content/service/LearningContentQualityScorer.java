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

    private final ContentSourceQualityProperties sourceQualityProperties;
    private final LearningContentTextSanitizer textSanitizer;

    /**
     * 후보 목록 전체를 점수화하고, 높은 점수 순서로 정렬한다.
     *
     * 이 메서드의 결과는 BraveSearchLearningContentGenerator에서
     * accepted() == true인 첫 번째 후보를 선택하는 데 사용된다.
     */
    public List<ScoredCandidate> score(List<LearningContentCandidate> candidates, StudyLanguage studyLanguage, LearningStyle learningStyle) {

         // hostCounts는 도메인별 등장 횟수를 세고, duplicatePenalty()에서 감점에 사용한다.
        Map<String, Integer> hostCounts = new HashMap<>();
        List<ScoredCandidate> scoredCandidates = candidates.stream()
                .map(candidate -> scoreCandidate(candidate, studyLanguage, learningStyle, hostCounts))
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
     * 4. 언어/출처/snippet/스타일/최신성 점수 합산
     * 5. 같은 도메인 반복 패널티 차감
     * 6. 기준 점수 미만이면 rejectReason 지정
     */
    private ScoredCandidate scoreCandidate(LearningContentCandidate candidate, StudyLanguage studyLanguage, LearningStyle learningStyle, Map<String, Integer> hostCounts) {
        // sourceHost가 없으면 URL에서 host를 뽑아낸다. 예: https://www.bbc.com/news -> bbc.com
        String host = normalizeHost(candidate.sourceHost());
        if (!StringUtils.hasText(host)) {
            host = normalizeHost(candidate.url());
        }

         // SNS/동영상/이미지 중심 도메인은 학습용 원문으로 부적합하다고 보고 바로 탈락시킨다. ex) x.com, youtube.com, instagram.com 등
        if (sourceQualityProperties.isBlocked(host)) {
            return new ScoredCandidate(candidate, 0, "blocked_domain");
        }

        // Brave description/extra_snippets/LLM Context snippet을 정제해서 실제 sourceText 후보를 만든다. (HTML 태그, entity, 너무 짧은 문장, 깨진 snippet은 여기서 걸러진다.)
        String sourceText = textSanitizer.sanitizeSourceText(candidate.snippets());
        if (!textSanitizer.isUsableSourceText(sourceText)) {
            return new ScoredCandidate(candidate, 0, "unusable_snippet");
        }

        // "쓸 수는 있는 후보"에 점수를 더하는 단계다. 총점은 100점 만점에 가깝고, 이후 중복 도메인 패널티가 차감된다.
        int score = 0;
        score += languageScore(candidate, studyLanguage, sourceText);
        score += sourceScore(host);
        score += snippetRichnessScore(candidate);
        score += styleFitScore(candidate, learningStyle, sourceText);
        score += freshnessScore(candidate, learningStyle);

        int duplicatePenalty = duplicatePenalty(host, hostCounts);
        score = Math.max(0, score - duplicatePenalty);
        String rejectReason = score >= MIN_ACCEPTED_SCORE ? null : "score_below_threshold";
        return new ScoredCandidate(candidate, score, rejectReason);
    }

    /*
     * 학습하려는 언어와 후보 콘텐츠의 언어가 맞는지 확인한다.
     *
     * 1순위: Brave 응답의 language 필드가 있으면 그 값을 사용한다.
     * 2순위: language 필드가 없거나 맞지 않으면 sourceText 문자 패턴으로 대략 판정한다.
     *
     * 예:
     * - 영어: 알파벳 단어가 있는지
     * - 일본어: 히라가나/가타카나/한자가 있는지
     * - 중국어: CJK 한자가 있는지
     */
    private int languageScore(LearningContentCandidate candidate, StudyLanguage studyLanguage, String sourceText) {
        String language = candidate.language();
        if (StringUtils.hasText(language)
                && language.toLowerCase(Locale.ROOT).startsWith(studyLanguage.getContentLanguageCode())) {
            return 25;
        }

        return switch (studyLanguage) {
            case ENGLISH -> sourceText.matches(".*[A-Za-z]{3,}.*") ? 18 : 0;
            case JAPANESE -> sourceText.matches(".*[ぁ-んァ-ヶ一-龯].*") ? 18 : 0;
            case CHINESE -> sourceText.matches(".*[\\u4E00-\\u9FFF].*") ? 18 : 0;
        };
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
     * 후보가 가진 snippet이 학습 콘텐츠를 만들 만큼 충분한지 본다.
     * description 하나만 짧게 있는 후보보다, extra_snippets나 LLM Context로 본문 조각이 많은 후보를 높게 평가한다.
     */
    private int snippetRichnessScore(LearningContentCandidate candidate) {
        int totalLength = candidate.snippets().stream()
                .filter(StringUtils::hasText)
                .mapToInt(String::length)
                .sum();
        if (totalLength >= 400) {
            return 20;
        }
        if (totalLength >= 180) {
            return 16;
        }
        if (totalLength >= 80) {
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
}
