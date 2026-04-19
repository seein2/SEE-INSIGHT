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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningContentQualityScorer {

    public static final int MIN_ACCEPTED_SCORE = 60;

    private final ContentSourceQualityProperties sourceQualityProperties;
    private final LearningContentTextSanitizer textSanitizer;

    /**
     * 후보 목록을 점수순으로 정렬
     */
    public List<ScoredCandidate> score(
            List<LearningContentCandidate> candidates,
            StudyLanguage studyLanguage,
            LearningStyle learningStyle
    ) {
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

    private ScoredCandidate scoreCandidate(
            LearningContentCandidate candidate,
            StudyLanguage studyLanguage,
            LearningStyle learningStyle,
            Map<String, Integer> hostCounts
    ) {
        String host = normalizeHost(candidate.sourceHost());
        if (!StringUtils.hasText(host)) {
            host = normalizeHost(candidate.url());
        }

        if (sourceQualityProperties.isBlocked(host)) {
            return new ScoredCandidate(candidate, 0, "blocked_domain");
        }

        String sourceText = textSanitizer.sanitizeSourceText(candidate.snippets());
        if (!textSanitizer.isUsableSourceText(sourceText)) {
            return new ScoredCandidate(candidate, 0, "unusable_snippet");
        }

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

    private int sourceScore(String host) {
        if (!StringUtils.hasText(host)) {
            return 8;
        }
        return sourceQualityProperties.isTrusted(host) ? 20 : 12;
    }

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

    private int freshnessScore(LearningContentCandidate candidate, LearningStyle learningStyle) {
        if (learningStyle == LearningStyle.TODAYS_EXPRESSION) {
            return 15;
        }

        if (Boolean.TRUE.equals(candidate.breaking())) {
            return 15;
        }

        return StringUtils.hasText(candidate.pageAge()) ? 14 : 8;
    }

    private int duplicatePenalty(String host, Map<String, Integer> hostCounts) {
        if (!StringUtils.hasText(host)) {
            return 0;
        }

        int count = hostCounts.getOrDefault(host, 0);
        hostCounts.put(host, count + 1);
        return Math.min(30, count * 15);
    }

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

    public record ScoredCandidate(LearningContentCandidate candidate, int score, String rejectReason) {

        public boolean accepted() {
            return rejectReason == null;
        }
    }
}
