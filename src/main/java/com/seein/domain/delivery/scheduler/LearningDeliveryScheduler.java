package com.seein.domain.delivery.scheduler;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.delivery.entity.DeliveryLog;
import com.seein.domain.delivery.entity.DeliveryStatus;
import com.seein.domain.delivery.repository.DeliveryLogRepository;
import com.seein.domain.delivery.service.LearningEmailService;
import com.seein.domain.subscription.entity.LearningSubscription;
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

    /**
     * 일일 학습 이메일 발송
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void sendDailyLearningDigest() {
        sendDailyLearningDigest(LocalDateTime.now());
    }

    void sendDailyLearningDigest(LocalDateTime issueDateTime) {
        LocalDateTime normalizedIssueDateTime = issueDateTime.withSecond(0).withNano(0);
        LocalTime deliveryTime = normalizedIssueDateTime.toLocalTime();
        LocalDate issueDate = normalizedIssueDateTime.toLocalDate();
        List<LearningSubscription> dueSubscriptions = subscriptionRepository.findDeliverableSubscriptions(deliveryTime);
        Map<String, LearningContent> contentCache = new HashMap<>();

        if (dueSubscriptions.isEmpty()) {
            log.debug("학습 이메일 발송 대상 없음 - deliveryTime={}, issueDate={}", deliveryTime, issueDate);
            return;
        }

        for (LearningSubscription subscription : dueSubscriptions) {
            try {
                if (deliveryLogRepository.existsBySubscriptionSubscriptionIdAndStatusAndIssueDate(subscription.getSubscriptionId(), DeliveryStatus.SUCCESS, issueDate)) {
                    continue;
                }

                String cacheKey = createContentCacheKey(subscription, issueDate);

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
                saveFailureLog(subscription, issueDate, e);
            }
        }
    }

    /*
     * 같은 설정의 구독은 하루에 동일 콘텐츠를 사용하므로 배치 실행 안에서만 캐싱한다.
     */
    private String createContentCacheKey(LearningSubscription subscription, LocalDate issueDate) {
        return String.join("|",
                subscription.getStudyLanguage().name(),
                subscription.getExplanationLanguage().name(),
                subscription.getLearningStyle().name(),
                subscription.getDifficultyLevel().name(),
                issueDate.toString()
        );
    }

    /*
     * 실패 로그 저장 실패가 이후 구독 발송까지 중단하지 않도록 별도로 보호한다.
     */
    private void saveFailureLog(LearningSubscription subscription, LocalDate issueDate, Exception exception) {
        try {
            deliveryLogRepository.save(DeliveryLog.createFailure(subscription, null, issueDate, exception.getMessage()));
        } catch (Exception logException) {
            log.error("학습 이메일 실패 로그 저장 실패 - subscriptionId={}, issueDate={}, error={}",
                    subscription.getSubscriptionId(), issueDate, logException.getMessage(), logException);
        }
    }
}
