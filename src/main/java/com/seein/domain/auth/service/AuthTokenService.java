package com.seein.domain.auth.service;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.dto.TokenResponse;
import com.seein.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 인증 토큰 발급 및 재발급 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 이메일 기준으로 Access/Refresh Token 발급
     */
    public AuthTokens issueTokens(String email) {
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        return AuthTokens.of(accessToken, refreshToken);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public TokenResponse refresh(String authorizationHeader) {
        String refreshToken = extractBearerToken(authorizationHeader);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        return TokenResponse.of(newAccessToken, refreshToken);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
