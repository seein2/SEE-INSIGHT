package com.seein.domain.auth.service;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.dto.RefreshTokenSession;
import com.seein.domain.auth.repository.RefreshTokenRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import com.seein.global.security.jwt.JwtProperties;
import com.seein.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 인증 토큰 발급 및 재발급 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHashService refreshTokenHashService;

    /**
     * 이메일 기준으로 Access/Refresh Token 발급
     */
    @Transactional
    public AuthTokens issueTokens(Integer memberId, String email) {
        String refreshTokenId = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(memberId, email);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId, email, refreshTokenId);

        registerRefreshToken(memberId, refreshTokenId, refreshToken);
        return AuthTokens.of(accessToken, refreshToken, refreshTokenId);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Claims claims = jwtTokenProvider.getValidatedRefreshClaims(refreshToken);
        Integer memberId = claims.get("member_id", Integer.class);
        String tokenId = claims.getId();
        String email = claims.getSubject();

        if (memberId == null || !StringUtils.hasText(tokenId) || !StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshTokenSession storedSession = refreshTokenRepository.find(memberId, tokenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        String tokenHash = refreshTokenHashService.hash(refreshToken);
        if (!Objects.equals(storedSession.getTokenHash(), tokenHash)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        refreshTokenRepository.delete(storedSession);
        return issueTokens(memberId, email);
    }

    /**
     * 로그아웃 시 Refresh Token 무효화
     */
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }

        try {
            Claims claims = jwtTokenProvider.getRefreshClaimsAllowExpired(refreshToken);
            Integer memberId = claims.get("member_id", Integer.class);
            String tokenId = claims.getId();

            if (memberId != null && StringUtils.hasText(tokenId)) {
                refreshTokenRepository.delete(memberId, tokenId);
            }
        } catch (BusinessException e) {
            // 로그아웃은 best-effort 무효화 처리
        }
    }

    /**
     * Refresh Token 세션 등록 (로그인 또는 재발급 시)
     */
    private void registerRefreshToken(Integer memberId, String refreshTokenId, String refreshToken) {
        RefreshTokenSession session = RefreshTokenSession.of(
                memberId,
                refreshTokenId,
                refreshTokenHashService.hash(refreshToken),
                LocalDateTime.now().plusNanos(jwtProperties.getRefreshExpiration() * 1_000_000L)
        );
        refreshTokenRepository.save(session);
    }
}
