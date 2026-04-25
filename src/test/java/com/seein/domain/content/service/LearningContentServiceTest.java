package com.seein.domain.content.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.repository.LearningContentRepository;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningContentServiceTest {

    @InjectMocks
    private LearningContentService learningContentService;

    @Mock
    private LearningContentRepository learningContentRepository;

    @Mock
    private LearningContentGenerator learningContentGenerator;

    @Mock
    private LearningContentFallbackFactory fallbackFactory;

    @Mock
    private LearningContentTemplateFactory templateFactory;

    @Test
    @DisplayName("홈 피드 조회는 저장된 콘텐츠만 반환하고 런타임 fallback을 병합하지 않는다")
    void getFeedCards_returnsOnlyStoredContents() {
        // given
        LearningContent content = createContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalDate.of(2026, 4, 1)
        );
        given(learningContentRepository.findByStudyLanguageAndExplanationLanguage(
                eq(StudyLanguage.ENGLISH),
                eq(ExplanationLanguage.KOREAN),
                any(PageRequest.class)
        )).willReturn(new PageImpl<>(java.util.List.of(content)));
        given(templateFactory.createExplanation(
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER
        )).willReturn("계산된 학습 포인트");
        given(templateFactory.createQuiz(
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED
        )).willReturn("계산된 복습");

        // when
        java.util.List<LearningContentCardResponse> cards = learningContentService.getFeedCards(StudyLanguage.ENGLISH, null);

        // then
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getTitle()).isEqualTo("학습 제목");
        assertThat(cards.get(0).getExplanationText()).isEqualTo("계산된 학습 포인트");
        assertThat(cards.get(0).getQuizText()).isEqualTo("계산된 복습");
        verify(fallbackFactory, never()).createDailyContent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("학습 콘텐츠 생성이 실패하면 fallback 콘텐츠를 저장한다")
    void getOrCreateDailyContent_fallbackOnGenerationFailure() {
        // given
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        LearningContent fallbackContent = createContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER,
                publishedDate
        );
        given(learningContentRepository
                .findByStudyLanguageAndExplanationLanguageAndLearningStyleAndDifficultyLevelAndPublishedDate(
                        StudyLanguage.ENGLISH,
                        ExplanationLanguage.KOREAN,
                        LearningStyle.DAILY_CONVERSATION,
                        DifficultyLevel.BEGINNER,
                        publishedDate
                )).willReturn(Optional.empty());
        given(learningContentGenerator.generate(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER,
                publishedDate
        )).willThrow(new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED));
        given(fallbackFactory.createDailyContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER,
                publishedDate
        )).willReturn(fallbackContent);
        given(learningContentRepository.save(fallbackContent)).willReturn(fallbackContent);

        // when
        LearningContent content = learningContentService.getOrCreateDailyContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER,
                publishedDate
        );

        // then
        assertThat(content).isSameAs(fallbackContent);
        verify(learningContentRepository).save(fallbackContent);
    }

    private LearningContent createContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        return LearningContent.create(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                "학습 제목",
                "원문",
                "표현1",
                "표현2",
                "https://example.com",
                publishedDate
        );
    }
}
