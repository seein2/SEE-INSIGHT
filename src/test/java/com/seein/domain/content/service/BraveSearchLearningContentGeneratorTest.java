package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.config.BraveSearchClient;
import com.seein.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private BraveSearchClient braveSearchClient;

    @Mock
    private BraveSearchQueryFactory braveSearchQueryFactory;

    @Test
    @DisplayName("Brave 검색 결과를 학습 콘텐츠 엔티티로 변환한다")
    void generate_success() {
        // given
        BraveSearchLearningContentGenerator generator = new BraveSearchLearningContentGenerator(
                braveSearchClient,
                braveSearchQueryFactory,
                new LearningContentTemplateFactory()
        );
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        given(braveSearchQueryFactory.create(
                StudyLanguage.ENGLISH,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        )).willReturn(new BraveSearchQueryFactory.SearchQuery("query", "pd", "pw"));
        given(braveSearchClient.searchWeb("query", "en", "pd")).willReturn(List.of(
                new BraveSearchClient.SearchResult(
                        "Daily habits | Example News",
                        "A small daily habit often matters more than a perfect long plan.",
                        "https://example.com/article"
                )
        ));

        // when
        LearningContent content = generator.generate(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        );

        // then
        assertThat(content.getTitle()).isEqualTo("Daily habits");
        assertThat(content.getSourceText()).contains("small daily habit");
        assertThat(content.getSourceLink()).isEqualTo("https://example.com/article");
        assertThat(content.getSummary()).contains("Daily habits");
        assertThat(content.getExpressionOne()).isNotBlank();
    }

    @Test
    @DisplayName("당일 검색 결과가 없으면 더 넓은 freshness로 한 번 더 조회한다")
    void generate_retryWithFallbackFreshness() {
        // given
        BraveSearchLearningContentGenerator generator = new BraveSearchLearningContentGenerator(
                braveSearchClient,
                braveSearchQueryFactory,
                new LearningContentTemplateFactory()
        );
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        given(braveSearchQueryFactory.create(
                StudyLanguage.ENGLISH,
                LearningStyle.PRACTICAL_READING,
                DifficultyLevel.INTERMEDIATE,
                publishedDate
        )).willReturn(new BraveSearchQueryFactory.SearchQuery("query", "pd", "pw"));
        given(braveSearchClient.searchWeb("query", "en", "pd")).willReturn(List.of());
        given(braveSearchClient.searchWeb("query", "en", "pw")).willReturn(List.of(
                new BraveSearchClient.SearchResult("Reading practice", "Recent short article for learners.", "https://example.com")
        ));

        // when
        LearningContent content = generator.generate(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.PRACTICAL_READING,
                DifficultyLevel.INTERMEDIATE,
                publishedDate
        );

        // then
        assertThat(content.getTitle()).isEqualTo("Reading practice");
        verify(braveSearchClient).searchWeb("query", "en", "pd");
        verify(braveSearchClient).searchWeb("query", "en", "pw");
    }

    @Test
    @DisplayName("유효한 Brave 검색 결과가 없으면 생성 실패 예외를 던진다")
    void generate_failWhenNoUsableResults() {
        // given
        BraveSearchLearningContentGenerator generator = new BraveSearchLearningContentGenerator(
                braveSearchClient,
                braveSearchQueryFactory,
                new LearningContentTemplateFactory()
        );
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        given(braveSearchQueryFactory.create(
                StudyLanguage.JAPANESE,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED,
                publishedDate
        )).willReturn(new BraveSearchQueryFactory.SearchQuery("query", "pd", "pw"));
        given(braveSearchClient.searchWeb("query", "ja", "pd")).willReturn(List.of(
                new BraveSearchClient.SearchResult(null, "설명만 있음", "https://example.com")
        ));
        given(braveSearchClient.searchWeb("query", "ja", "pw")).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> generator.generate(
                StudyLanguage.JAPANESE,
                ExplanationLanguage.KOREAN,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED,
                publishedDate
        )).isInstanceOf(BusinessException.class);
    }
}
