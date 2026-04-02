package com.seein.domain.delivery.scheduler;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.delivery.entity.DeliveryLog;
import com.seein.domain.delivery.entity.DeliveryStatus;
import com.seein.domain.delivery.repository.DeliveryLogRepository;
import com.seein.domain.delivery.service.LearningEmailService;
import com.seein.domain.member.entity.Member;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.domain.subscription.entity.LearningSubscription;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningDeliverySchedulerTest {

    @InjectMocks
    private LearningDeliveryScheduler learningDeliveryScheduler;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private LearningContentService learningContentService;

    @Mock
    private LearningEmailService learningEmailService;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Test
    @DisplayName("발송 시간과 일치하는 활성 구독에 학습 메일을 보낸다")
    void sendDailyLearningDigest_success() throws Exception {
        // given
        LocalDateTime issueDateTime = LocalDateTime.of(2026, 4, 1, 8, 0);
        LearningSubscription subscription = createSubscription(LocalTime.of(8, 0));
        LearningContent content = LearningContent.create(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                "제목",
                "요약",
                "원문",
                "해설",
                "표현1",
                "표현2",
                "질문",
                "https://example.com",
                issueDateTime.toLocalDate()
        );

        given(subscriptionRepository.findDeliverableSubscriptions(LocalTime.of(8, 0))).willReturn(List.of(subscription));
        given(deliveryLogRepository.existsBySubscriptionSubscriptionIdAndStatusAndIssueDate(
                eq(subscription.getSubscriptionId()),
                eq(DeliveryStatus.SUCCESS),
                eq(issueDateTime.toLocalDate())
        )).willReturn(false);
        given(learningContentService.getOrCreateDailyContent(
                subscription.getStudyLanguage(),
                subscription.getExplanationLanguage(),
                subscription.getLearningStyle(),
                subscription.getDifficultyLevel(),
                issueDateTime.toLocalDate()
        )).willReturn(content);

        // when
        learningDeliveryScheduler.sendDailyLearningDigest(issueDateTime);

        // then
        verify(learningEmailService).sendLearningEmail(subscription, content);
        verify(deliveryLogRepository).save(any(DeliveryLog.class));
    }

    @Test
    @DisplayName("같은 날 이미 발송된 구독은 건너뛴다")
    void sendDailyLearningDigest_skipAlreadySent() throws Exception {
        // given
        LocalDateTime issueDateTime = LocalDateTime.of(2026, 4, 1, 8, 0);
        LearningSubscription subscription = createSubscription(LocalTime.of(8, 0));
        given(subscriptionRepository.findDeliverableSubscriptions(LocalTime.of(8, 0))).willReturn(List.of(subscription));
        given(deliveryLogRepository.existsBySubscriptionSubscriptionIdAndStatusAndIssueDate(
                eq(subscription.getSubscriptionId()),
                eq(DeliveryStatus.SUCCESS),
                eq(LocalDate.of(2026, 4, 1))
        )).willReturn(true);

        // when
        learningDeliveryScheduler.sendDailyLearningDigest(issueDateTime);

        // then
        verify(learningContentService, never()).getOrCreateDailyContent(any(), any(), any(), any(), any());
        verify(learningEmailService, never()).sendLearningEmail(any(), any());
    }

    private LearningSubscription createSubscription(LocalTime deliveryTime) {
        Member member = Member.create("test@example.com", "테스터", "google");
        LearningSubscription subscription = LearningSubscription.create(
                member,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                deliveryTime
        );
        ReflectionTestUtils.setField(subscription, "subscriptionId", 1);
        return subscription;
    }
}
