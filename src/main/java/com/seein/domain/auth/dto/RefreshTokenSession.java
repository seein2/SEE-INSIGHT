package com.seein.domain.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis에 저장되는 Refresh Token 세션 정보
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenSession {

    private Integer memberId;
    private String tokenId;
    private String tokenHash;
    private LocalDateTime expiresAt;

    public static RefreshTokenSession of(Integer memberId, String tokenId, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshTokenSession(memberId, tokenId, tokenHash, expiresAt);
    }
}
