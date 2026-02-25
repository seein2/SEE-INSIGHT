package com.seein.domain.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토큰 응답 DTO
 * Access Token과 Refresh Token을 함께 반환
 */
@Getter
@RequiredArgsConstructor
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
