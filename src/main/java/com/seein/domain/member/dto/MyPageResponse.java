package com.seein.domain.member.dto;

import com.seein.domain.subscription.dto.SubscriptionResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마이 페이지 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class MyPageResponse {

    private final Integer memberId;
    private final String email;
    private final String nickname;
    private final String membership;
    private final String membershipLabel;
    private final String provider;
    private final LocalDateTime createdAt;
    private final long currentSubscriptionCount;
    private final String subscriptionLimitLabel;
    private final boolean premium;
    private final List<SubscriptionResponse> subscriptions;
}
