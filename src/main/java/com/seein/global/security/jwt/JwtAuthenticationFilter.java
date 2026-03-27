package com.seein.global.security.jwt;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.service.AuthTokenService;
import com.seein.global.exception.BusinessException;
import com.seein.global.security.cookie.AuthCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 요청 헤더에서 JWT 토큰을 추출하고 검증하여 SecurityContext에 인증 정보 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;

    /**
     * 요청에서 Access Token과 Refresh Token을 추출하여 유효성 검사
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveCookieToken(request, AuthCookieService.ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = resolveCookieToken(request, AuthCookieService.REFRESH_TOKEN_COOKIE_NAME);
        String bearerToken = resolveBearerToken(request);

        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateAccessToken(accessToken)) {
            authenticate(accessToken);
        } else if (StringUtils.hasText(bearerToken) && jwtTokenProvider.validateAccessToken(bearerToken)) {
            authenticate(bearerToken);
        } else if (!StringUtils.hasText(bearerToken) && StringUtils.hasText(refreshToken)) {
            tryAutoRefresh(request, response, refreshToken);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return "/api/v1/auth/refresh".equals(requestUri);
    }

    /**
     * 요청 쿠키 또는 헤더에서 토큰 정보 추출
     */
    private String resolveCookieToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 요청 헤더에서 Bearer 토큰 추출
     */
    private String resolveBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Access Token이 만료된 경우 Refresh Token으로 자동 재발급 시도
     */
    private void tryAutoRefresh(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        try {
            AuthTokens authTokens = authTokenService.refresh(refreshToken);
            authCookieService.addAuthCookies(response, authTokens, request.isSecure());
            authenticate(authTokens.getAccessToken());
            log.debug("Refresh token으로 Access Token을 재발급했습니다.");
        } catch (BusinessException e) {
            authTokenService.invalidateRefreshToken(refreshToken);
            authCookieService.clearAuthCookies(response, request.isSecure());
            SecurityContextHolder.clearContext();
            log.debug("자동 토큰 재발급에 실패했습니다: {}", e.getMessage());
        }
    }

    /**
     * Access Token으로 인증 정보를 Security Context에 저장
     */
    private void authenticate(String accessToken) {
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Security Context에 '{}' 인증 정보를 저장했습니다.", authentication.getName());
    }
}
