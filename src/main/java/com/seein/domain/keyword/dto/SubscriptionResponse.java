package com.seein.domain.keyword.dto;

import com.seein.domain.keyword.entity.Subscription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class SubscriptionResponse {

    private final Integer subscriptionId;
    private final Integer keywordId;
    private final String keyword;
    private final Boolean isActive;
    private final String notificationTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getKeyword().getKeywordId(),
                subscription.getKeyword().getKeyword(),
                subscription.getIsActive(),
                subscription.getNotificationTime(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
