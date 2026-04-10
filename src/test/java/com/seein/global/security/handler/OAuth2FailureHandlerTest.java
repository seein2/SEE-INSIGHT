package com.seein.global.security.handler;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2FailureHandlerTest {

    @Test
    @DisplayName("OAuth2 로그인 실패 시 허용된 에러 코드만 로그인 페이지로 리다이렉트한다")
    void onAuthenticationFailure_redirectsWithSanitizedErrorCode() throws ServletException, IOException {
        // given
        OAuth2FailureHandler handler = new OAuth2FailureHandler("/login");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("민감한 내부 오류"));

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?oauthError=oauth2_authentication_failed");
    }

    @Test
    @DisplayName("기존 쿼리 파라미터가 있어도 OAuth2 에러 코드를 안전하게 추가한다")
    void onAuthenticationFailure_preservesExistingQueryParams() throws ServletException, IOException {
        // given
        OAuth2FailureHandler handler = new OAuth2FailureHandler("/login?from=oauth2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception =
                new OAuth2AuthenticationException(new OAuth2Error("access_denied"), "사용자 동의 취소");

        // when
        handler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?from=oauth2&oauthError=oauth2_access_denied");
    }
}
