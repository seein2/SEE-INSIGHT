package com.seein.domain.keyword.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 키워드 구독 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SubscriptionCreateRequest {

    @NotBlank(message = "키워드는 필수 입력입니다.")
    private String keyword;
}
