package com.seein.global.security.cookie;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.global.security.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 인증 쿠키 생성 서비스
 */
@Component
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final JwtProperties jwtProperties;

    public void addAuthCookies(HttpServletResponse response, AuthTokens authTokens, boolean secureRequest) {
        addCookie(response, ACCESS_TOKEN_COOKIE_NAME, authTokens.getAccessToken(), jwtProperties.getAccessExpiration(), secureRequest);
        addCookie(response, REFRESH_TOKEN_COOKIE_NAME, authTokens.getRefreshToken(), jwtProperties.getRefreshExpiration(), secureRequest);
    }

    public void clearAuthCookies(HttpServletResponse response, boolean secureRequest) {
        expireCookie(response, ACCESS_TOKEN_COOKIE_NAME, secureRequest);
        expireCookie(response, REFRESH_TOKEN_COOKIE_NAME, secureRequest);
    }

    private void addCookie(HttpServletResponse response, String cookieName, String token, long expirationMillis, boolean secureRequest) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secureRequest)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(expirationMillis))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireCookie(HttpServletResponse response, String cookieName, boolean secureRequest) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secureRequest)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
