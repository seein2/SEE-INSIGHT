package com.seein.domain.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증 토큰 묶음
 * 인증 유스케이스에서 발급된 Access/Refresh Token 전달용
 */
@Getter
@RequiredArgsConstructor
public class AuthTokens {

    private final String accessToken;
    private final String refreshToken;
    private final String refreshTokenId;

    public static AuthTokens of(String accessToken, String refreshToken, String refreshTokenId) {
        return new AuthTokens(accessToken, refreshToken, refreshTokenId);
    }
}
