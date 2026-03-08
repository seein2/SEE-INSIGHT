package com.seein.domain.keyword.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 설정 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SubscriptionUpdateRequest {

    private Boolean isActive;
    private String notificationTime;
}
