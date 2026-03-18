package com.seein.global.security.handler;

import com.seein.global.security.jwt.JwtProperties;
import com.seein.global.security.jwt.JwtTokenProvider;
import com.seein.global.security.jwt.MemberPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * OAuth2 로그인 성공 핸들러
 * JWT 토큰 발급 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    private final String redirectUri = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        MemberPrincipal oAuth2User = (MemberPrincipal) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        log.info("OAuth2 로그인 성공: email={}, memberId={}", email, oAuth2User.getMemberId());

        // JWT를 HttpOnly 쿠키로 내려 브라우저가 이후 요청마다 자동 전송하도록 처리
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, jwtProperties.getAccessExpiration(), request.isSecure());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, jwtProperties.getRefreshExpiration(), request.isSecure());

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    /**
     * JWT 쿠키 생성
     */
    private void addTokenCookie(HttpServletResponse response, String cookieName, String token, long expirationMillis, boolean secureRequest) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secureRequest)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(expirationMillis))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
