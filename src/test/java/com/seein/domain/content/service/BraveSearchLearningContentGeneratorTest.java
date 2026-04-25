package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BraveSearchLearningContentGeneratorTest {

    @InjectMocks
    private BraveSearchLearningContentGenerator generator;

    @Mock
    private BraveSearchQueryFactory braveSearchQueryFactory;

    @Mock
    private LearningContentCandidateCollector candidateCollector;

    @Mock
    private LearningContentQualityScorer qualityScorer;

    @Mock
    private LearningContentCardAssembler cardAssembler;

    @Test
    @DisplayName("점수 기준을 통과한 후보를 학습 콘텐츠로 조립한다")
    void generate_success() {
        // given
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        BraveSearchQueryFactory.SearchQuery searchQuery = createSearchQuery();
        LearningContentCandidate candidate = createCandidate("https://example.com/article");
        LearningContentQualityScorer.ScoredCandidate scoredCandidate =
                new LearningContentQualityScorer.ScoredCandidate(candidate, 82, null);
        LearningContent assembledContent = LearningContent.createWithMetadata(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                "Daily habits",
                "A small daily habit often matters more than a perfect long plan.",
                "daily habit often matters",
                null,
                "https://example.com/article",
                ContentSourceType.WEB,
                "Example News",
                "example.com",
                "2 days ago",
                82,
                "raw",
                "brave-v2",
                publishedDate
        );
        given(braveSearchQueryFactory.create(
                StudyLanguage.ENGLISH,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        )).willReturn(searchQuery);
        given(candidateCollector.collect(searchQuery)).willReturn(List.of(candidate));
        given(qualityScorer.score(List.of(candidate), StudyLanguage.ENGLISH, LearningStyle.BALANCED))
                .willReturn(List.of(scoredCandidate));
        given(cardAssembler.assemble(
                scoredCandidate,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        )).willReturn(assembledContent);

        // when
        LearningContent content = generator.generate(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        );

        // then
        assertThat(content).isSameAs(assembledContent);
        assertThat(content.getQualityScore()).isEqualTo(82);
        verify(candidateCollector).collect(searchQuery);
        verify(cardAssembler).assemble(
                scoredCandidate,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        );
    }

    @Test
    @DisplayName("품질 기준을 통과한 후보가 없으면 생성 실패 예외를 던진다")
    void generate_failWhenNoAcceptedCandidate() {
        // given
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        BraveSearchQueryFactory.SearchQuery searchQuery = createSearchQuery();
        LearningContentCandidate candidate = createCandidate("https://example.com/bad");
        given(braveSearchQueryFactory.create(
                StudyLanguage.JAPANESE,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED,
                publishedDate
        )).willReturn(searchQuery);
        given(candidateCollector.collect(searchQuery)).willReturn(List.of(candidate));
        given(qualityScorer.score(List.of(candidate), StudyLanguage.JAPANESE, LearningStyle.TODAYS_EXPRESSION))
                .willReturn(List.of(new LearningContentQualityScorer.ScoredCandidate(candidate, 40, "score_below_threshold")));

        // when & then
        assertThatThrownBy(() -> generator.generate(
                StudyLanguage.JAPANESE,
                ExplanationLanguage.KOREAN,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED,
                publishedDate
        )).isInstanceOf(BusinessException.class);
    }

    private BraveSearchQueryFactory.SearchQuery createSearchQuery() {
        return new BraveSearchQueryFactory.SearchQuery(
                "query",
                "US",
                "en",
                "en-US",
                ContentSourceType.WEB,
                "pw",
                "pm",
                true,
                8
        );
    }

    private LearningContentCandidate createCandidate(String url) {
        return new LearningContentCandidate(
                ContentSourceType.WEB,
                "Daily habits",
                url,
                "A small daily habit often matters more than a perfect long plan.",
                List.of("A small daily habit often matters more than a perfect long plan."),
                "2 days ago",
                "en",
                "Example News",
                "example.com",
                "article",
                false
        );
    }
}
