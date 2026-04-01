package com.seein.domain.delivery.scheduler;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.delivery.entity.DeliveryLog;
import com.seein.domain.delivery.entity.DeliveryStatus;
import com.seein.domain.delivery.repository.DeliveryLogRepository;
import com.seein.domain.delivery.service.LearningEmailService;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 학습 이메일 발송 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningDeliveryScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final LearningContentService learningContentService;
    private final LearningEmailService learningEmailService;
    private final DeliveryLogRepository deliveryLogRepository;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void sendDailyLearningDigest() {
        sendDailyLearningDigest(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
    }

    void sendDailyLearningDigest(LocalDateTime issueDateTime) {
        LocalTime deliveryTime = issueDateTime.toLocalTime();
        LocalDate issueDate = issueDateTime.toLocalDate();
        List<Subscription> dueSubscriptions = subscriptionRepository.findDeliverableSubscriptions(deliveryTime);
        Map<String, LearningContent> contentCache = new HashMap<>();

        for (Subscription subscription : dueSubscriptions) {
            try {
                if (deliveryLogRepository.existsBySubscriptionSubscriptionIdAndStatusAndIssueDate(
                        subscription.getSubscriptionId(),
                        DeliveryStatus.SUCCESS,
                        issueDate
                )) {
                    continue;
                }

                String cacheKey = String.join("|",
                        subscription.getStudyLanguage().name(),
                        subscription.getExplanationLanguage().name(),
                        subscription.getLearningStyle().name(),
                        subscription.getDifficultyLevel().name(),
                        issueDate.toString()
                );

                LearningContent learningContent = contentCache.get(cacheKey);
                if (learningContent == null) {
                    learningContent = learningContentService.getOrCreateDailyContent(
                            subscription.getStudyLanguage(),
                            subscription.getExplanationLanguage(),
                            subscription.getLearningStyle(),
                            subscription.getDifficultyLevel(),
                            issueDate
                    );
                    contentCache.put(cacheKey, learningContent);
                }

                learningEmailService.sendLearningEmail(subscription, learningContent);
                deliveryLogRepository.save(DeliveryLog.createSuccess(subscription, learningContent, issueDate));
            } catch (Exception e) {
                log.error("학습 이메일 발송 실패 - subscriptionId={}, error={}",
                        subscription.getSubscriptionId(), e.getMessage(), e);
                deliveryLogRepository.save(DeliveryLog.createFailure(subscription, null, issueDate, e.getMessage()));
            }
        }
    }
}
