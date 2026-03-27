package com.seein.global.security.jwt;

import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String MEMBER_ID_CLAIM = "member_id";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String email) {
        return createAccessToken(null, email);
    }

    public String createAccessToken(Integer memberId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getAccessExpiration());

        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

        if (memberId != null) {
            jwtBuilder.claim(MEMBER_ID_CLAIM, memberId);
        }

        return jwtBuilder.signWith(secretKey).compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String email) {
        return createRefreshToken(null, email, UUID.randomUUID().toString());
    }

    public String createRefreshToken(Integer memberId, String email, String tokenId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .id(tokenId)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

        if (memberId != null) {
            jwtBuilder.claim(MEMBER_ID_CLAIM, memberId);
        }

        return jwtBuilder.signWith(secretKey).compact();
    }

    /**
     * JWT 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Integer getMemberIdFromToken(String token) {
        return getClaims(token).get(MEMBER_ID_CLAIM, Integer.class);
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public Claims getValidatedRefreshClaims(String token) {
        Claims claims = getClaims(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    /**
     * Refresh Token 검증 시 만료된 토큰도 허용하여 Claims 반환 (로그아웃 시 Redis에서 무효화하기 위함)
     */
    public Claims getRefreshClaimsAllowExpired(String token) {
        Claims claims = getClaimsAllowExpired(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (BusinessException e) {
            log.debug("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (BusinessException e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰으로부터 인증 객체 생성 (이메일 -> 회원 정보)
     */
    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Claims getClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
