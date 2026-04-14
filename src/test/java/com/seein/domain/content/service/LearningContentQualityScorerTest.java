package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LearningContentQualityScorerTest {

    private final LearningContentQualityScorer qualityScorer = new LearningContentQualityScorer(
            new ContentSourceQualityProperties(),
            new LearningContentTextSanitizer()
    );

    @Test
    @DisplayName("신뢰 도메인의 풍부한 뉴스 snippet은 품질 기준을 통과한다")
    void score_acceptsTrustedRichNewsCandidate() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.NEWS,
                "BBC short news article",
                "https://www.bbc.com/news/example",
                "bbc.com",
                "en",
                List.of("A local council said the new plan will help residents use buses more often, "
                        + "reduce traffic near schools, and make short trips easier for families this week."),
                "2 days ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.ENGLISH,
                LearningStyle.PRACTICAL_READING
        ).get(0);

        // then
        assertThat(scored.accepted()).isTrue();
        assertThat(scored.score()).isGreaterThanOrEqualTo(LearningContentQualityScorer.MIN_ACCEPTED_SCORE);
    }

    @Test
    @DisplayName("차단 도메인은 점수와 무관하게 reject된다")
    void score_rejectsBlockedDomain() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.WEB,
                "Social post",
                "https://x.com/example/status/1",
                "x.com",
                "en",
                List.of("This snippet is long enough to look usable, but the source domain should still be blocked by policy."),
                "1 day ago",
                "post"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.ENGLISH,
                LearningStyle.BALANCED
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("blocked_domain");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("언어 불일치와 스타일 부적합 후보는 기준 점수 미만으로 reject된다")
    void score_rejectsLanguageMismatchAndPoorStyleFit() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.WEB,
                "天気の話題",
                "https://example.com/jp",
                "example.com",
                "ja",
                List.of("今日は各地で雨が降り、駅の周辺では傘を持った人が多く見られました。通勤時間帯には交通機関の遅れもありました。"),
                "1 day ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.ENGLISH,
                LearningStyle.TODAYS_EXPRESSION
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("score_below_threshold");
    }

    @Test
    @DisplayName("같은 도메인의 반복 후보에는 중복 패널티를 적용한다")
    void score_appliesDuplicateHostPenalty() {
        // given
        LearningContentCandidate first = createCandidate(
                ContentSourceType.NEWS,
                "First article",
                "https://www.bbc.com/news/one",
                "bbc.com",
                "en",
                List.of("The first article explains a public transport change in clear language for local readers this week."),
                "1 day ago",
                "article"
        );
        LearningContentCandidate second = createCandidate(
                ContentSourceType.NEWS,
                "Second article",
                "https://www.bbc.com/news/two",
                "bbc.com",
                "en",
                List.of("The second article describes another public transport update in clear language for local readers this week."),
                "1 day ago",
                "article"
        );

        // when
        List<LearningContentQualityScorer.ScoredCandidate> scored = qualityScorer.score(
                List.of(first, second),
                StudyLanguage.ENGLISH,
                LearningStyle.PRACTICAL_READING
        );

        // then
        int firstScore = scoreByUrl(scored, "https://www.bbc.com/news/one");
        int secondScore = scoreByUrl(scored, "https://www.bbc.com/news/two");
        assertThat(secondScore).isLessThan(firstScore);
    }

    private int scoreByUrl(List<LearningContentQualityScorer.ScoredCandidate> scoredCandidates, String url) {
        return scoredCandidates.stream()
                .filter(scoredCandidate -> scoredCandidate.candidate().url().equals(url))
                .findFirst()
                .orElseThrow()
                .score();
    }

    private LearningContentCandidate createCandidate(
            ContentSourceType sourceType,
            String title,
            String url,
            String sourceHost,
            String language,
            List<String> snippets,
            String pageAge,
            String contentType
    ) {
        return new LearningContentCandidate(
                sourceType,
                title,
                url,
                snippets.get(0),
                snippets,
                pageAge,
                language,
                sourceHost,
                sourceHost,
                contentType,
                false
        );
    }
}
