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
                LearningStyle.NEWS_READING
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
    @DisplayName("언어 불일치 후보는 점수 합산 전에 reject된다")
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
        assertThat(scored.rejectReason()).isEqualTo("language_mismatch");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("명시 언어가 대상 학습 언어와 다르면 높은 품질 후보도 reject된다")
    void score_rejectsDeclaredLanguageMismatchEvenWhenCandidateLooksHighQuality() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.NEWS,
                "NHK news article",
                "https://www.nhk.or.jp/news/example",
                "nhk.or.jp",
                "ja",
                List.of("今日は政府が新しい交通政策を発表し、学校周辺の安全対策や通勤時間帯の混雑緩和について詳しく説明しました。地域の住民からは期待と不安の声が上がっています。"),
                "1 day ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.ENGLISH,
                LearningStyle.NEWS_READING
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("language_mismatch");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("언어 필드가 없어도 중국어 본문은 일본어 후보로 인정하지 않는다")
    void score_rejectsChineseTextAsJapaneseWhenLanguageIsMissing() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.NEWS,
                "China Daily article",
                "https://www.chinadaily.com.cn/example",
                "chinadaily.com.cn",
                null,
                List.of("中国政府今天公布新的城市交通计划，重点改善学校周边安全和公共汽车线路服务，居民表示将继续关注政策实施效果。"),
                "1 day ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.JAPANESE,
                LearningStyle.NEWS_READING
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("language_mismatch");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("언어 필드가 없어도 일본어 본문은 중국어 후보로 인정하지 않는다")
    void score_rejectsJapaneseTextAsChineseWhenLanguageIsMissing() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.NEWS,
                "Japanese local news",
                "https://www.asahi.com/example",
                "asahi.com",
                null,
                List.of("今日は各地で雨が降り、駅の周辺では傘を持った人が多く見られました。通勤時間帯には交通機関の遅れもありました。"),
                "1 day ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.CHINESE,
                LearningStyle.NEWS_READING
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("language_mismatch");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("언어 필드가 없을 때 일부 영어 단어만으로 영어 후보로 인정하지 않는다")
    void score_rejectsMostlyJapaneseTextAsEnglishWhenLanguageIsMissing() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.NEWS,
                "BBC article update",
                "https://example.com/mixed",
                "example.com",
                null,
                List.of("今日は政府が新しい計画を発表し、学校周辺の安全対策や交通機関の改善について詳しく説明しました。BBC article update."),
                "1 day ago",
                "article"
        );

        // when
        LearningContentQualityScorer.ScoredCandidate scored = qualityScorer.score(
                List.of(candidate),
                StudyLanguage.ENGLISH,
                LearningStyle.NEWS_READING
        ).get(0);

        // then
        assertThat(scored.accepted()).isFalse();
        assertThat(scored.rejectReason()).isEqualTo("language_mismatch");
        assertThat(scored.score()).isZero();
    }

    @Test
    @DisplayName("snippet 풍부함은 raw snippet 전체가 아니라 실제 사용 원문 길이를 기준으로 계산한다")
    void score_usesSanitizedSourceTextLengthForSnippetRichness() {
        // given
        LearningContentCandidate candidate = createCandidate(
                ContentSourceType.WEB,
                "Simple update",
                "https://example.com/simple",
                "example.com",
                "en",
                List.of(
                        "Daily habits help readers practice steady English with one clear example today.",
                        "This extra snippet is intentionally much longer than the selected source text. "
                                + "It should not inflate the richness score because the sanitizer uses the first usable source text."
                ),
                null,
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
        assertThat(scored.rejectReason()).isEqualTo("score_below_threshold");
        assertThat(scored.score()).isLessThan(LearningContentQualityScorer.MIN_ACCEPTED_SCORE);
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
                LearningStyle.NEWS_READING
        );

        // then
        int firstScore = scoreByUrl(scored, "https://www.bbc.com/news/one");
        int secondScore = scoreByUrl(scored, "https://www.bbc.com/news/two");
        assertThat(secondScore).isLessThan(firstScore);
    }

    @Test
    @DisplayName("중복 도메인 패널티는 검색 결과 순서가 아니라 원점수가 높은 후보부터 적용한다")
    void score_appliesDuplicateHostPenaltyAfterBaseScoreOrdering() {
        // given
        LearningContentCandidate lowerQualityFirst = createCandidate(
                ContentSourceType.NEWS,
                "Lower quality article",
                "https://www.bbc.com/news/lower",
                "bbc.com",
                "en",
                List.of("The first report explains a transport plan for local readers in simple English, with clear details for commuters today."),
                null,
                "article"
        );
        LearningContentCandidate higherQualitySecond = createCandidate(
                ContentSourceType.NEWS,
                "Higher quality article",
                "https://www.bbc.com/news/higher",
                "bbc.com",
                "en",
                List.of("The stronger report explains a transport policy change for local readers in simple English, with context about schools, bus routes, city budgets, public safety, and commuting patterns this week."),
                "1 day ago",
                "article"
        );

        // when
        List<LearningContentQualityScorer.ScoredCandidate> scored = qualityScorer.score(
                List.of(lowerQualityFirst, higherQualitySecond),
                StudyLanguage.ENGLISH,
                LearningStyle.NEWS_READING
        );

        // then
        int lowerScore = scoreByUrl(scored, "https://www.bbc.com/news/lower");
        int higherScore = scoreByUrl(scored, "https://www.bbc.com/news/higher");
        assertThat(higherScore).isGreaterThan(lowerScore);
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
