package com.seein.domain.subscription.dto;

import com.seein.domain.subscription.entity.Subscription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 구독 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class SubscriptionResponse {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Integer subscriptionId;
    private final String studyLanguage;
    private final String studyLanguageLabel;
    private final String explanationLanguage;
    private final String explanationLanguageLabel;
    private final String learningStyle;
    private final String learningStyleLabel;
    private final String difficultyLevel;
    private final String difficultyLevelLabel;
    private final Boolean isActive;
    private final String deliveryTime;
    private final String scheduleLabel;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getStudyLanguage().name(),
                subscription.getStudyLanguage().getLabel(),
                subscription.getExplanationLanguage().name(),
                subscription.getExplanationLanguage().getLabel(),
                subscription.getLearningStyle().name(),
                subscription.getLearningStyle().getLabel(),
                subscription.getDifficultyLevel().name(),
                subscription.getDifficultyLevel().getLabel(),
                subscription.getIsActive(),
                subscription.getDeliveryTime().format(TIME_FORMATTER),
                "매일 " + subscription.getDeliveryTime().format(TIME_FORMATTER),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
