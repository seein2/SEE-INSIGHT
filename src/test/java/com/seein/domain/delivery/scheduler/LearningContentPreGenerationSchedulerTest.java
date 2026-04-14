package com.seein.domain.delivery.scheduler;

import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.member.entity.Member;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.LearningSubscription;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningContentPreGenerationSchedulerTest {

    @InjectMocks
    private LearningContentPreGenerationScheduler scheduler;

    @Mock
    private LearningContentService learningContentService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("새벽 배치는 홈 기본 세트와 활성 구독 조합을 중복 없이 선생성한다")
    void preGenerateDailyLearningContent_generatesDefaultsAndDistinctSubscriptions() {
        // given
        LocalDate publishedDate = LocalDate.of(2026, 4, 1);
        LearningSubscription duplicatedHomePreset = createSubscription(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER
        );
        LearningSubscription uniqueSubscription = createSubscription(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.ENGLISH,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED
        );
        given(subscriptionRepository.findActiveSubscriptions()).willReturn(List.of(
                duplicatedHomePreset,
                uniqueSubscription,
                uniqueSubscription
        ));

        // when
        scheduler.preGenerateDailyLearningContent(publishedDate);

        // then
        verify(learningContentService, times(10))
                .getOrCreateDailyContent(any(), any(), any(), any(), eq(publishedDate));
        verify(learningContentService, times(1)).getOrCreateDailyContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                publishedDate
        );
        verify(learningContentService, times(1)).getOrCreateDailyContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.ENGLISH,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.ADVANCED,
                publishedDate
        );
    }

    private LearningSubscription createSubscription(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel
    ) {
        Member member = Member.create("test@example.com", "테스터", "google");
        return LearningSubscription.create(
                member,
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                LocalTime.of(8, 0)
        );
    }
}
