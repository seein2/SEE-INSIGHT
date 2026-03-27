package com.seein.global.security.handler;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.service.AuthTokenService;
import com.seein.global.security.cookie.AuthCookieService;
import com.seein.global.security.jwt.MemberPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * JWT 토큰 발급 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;

    private final String redirectUri = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        MemberPrincipal oAuth2User = (MemberPrincipal) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        AuthTokens authTokens = authTokenService.issueTokens(oAuth2User.getMemberId(), email);

        log.info("OAuth2 로그인 성공: email={}, memberId={}", email, oAuth2User.getMemberId());

        authCookieService.addAuthCookies(response, authTokens, request.isSecure());

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
