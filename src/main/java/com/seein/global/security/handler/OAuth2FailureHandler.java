package com.seein.global.security.handler;

import com.seein.global.security.oauth2.OAuth2LoginError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 실패 핸들러
 * 로그인 실패 시 에러 메시지와 함께 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_FAILURE_REDIRECT_URI = "/login";

    private final String failureRedirectUri;

    public OAuth2FailureHandler(@Value("${spring.security.oauth2.failure-redirect-uri}") String failureRedirectUri) {
        this.failureRedirectUri = failureRedirectUri;
    }

    /**
     * OAuth2 로그인 실패 처리
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        OAuth2LoginError loginError = OAuth2LoginError.fromException(exception);
        String targetUrl = UriComponentsBuilder.fromUriString(resolveFailureRedirectUri())
                .replaceQueryParam("oauthError", loginError.getCode())
                .build()
                .encode()
                .toUriString();

        log.warn("OAuth2 로그인 실패: code={}, message={}", loginError.getCode(), exception.getMessage(), exception);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveFailureRedirectUri() {
        return StringUtils.hasText(failureRedirectUri) ? failureRedirectUri : DEFAULT_FAILURE_REDIRECT_URI;
    }
}
