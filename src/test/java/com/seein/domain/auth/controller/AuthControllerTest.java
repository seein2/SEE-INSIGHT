package com.seein.domain.auth.controller;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.service.AuthTokenService;
import com.seein.domain.member.service.MemberService;
import com.seein.global.security.cookie.AuthCookieService;
import com.seein.global.security.handler.JwtAccessDeniedHandler;
import com.seein.global.security.handler.JwtAuthenticationEntryPoint;
import com.seein.global.security.handler.OAuth2FailureHandler;
import com.seein.global.security.handler.OAuth2SuccessHandler;
import com.seein.global.security.handler.RefreshTokenLogoutHandler;
import com.seein.global.security.jwt.JwtAuthenticationFilter;
import com.seein.global.security.jwt.JwtTokenProvider;
import com.seein.global.security.oauth2.OAuth2MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private OAuth2MemberService oAuth2MemberService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private RefreshTokenLogoutHandler refreshTokenLogoutHandler;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthTokenService authTokenService;

    @MockBean
    private AuthCookieService authCookieService;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("refresh_token 쿠키로 토큰을 재발급한다")
    void refresh_success() throws Exception {
        // given
        given(authTokenService.refresh("refresh-token"))
                .willReturn(AuthTokens.of("access-token", "new-refresh-token", "refresh-jti"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .cookie(new jakarta.servlet.http.Cookie(AuthCookieService.REFRESH_TOKEN_COOKIE_NAME, "refresh-token")))
                .andExpect(status().isOk());

        verify(authCookieService).addAuthCookies(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(AuthTokens.class), anyBoolean());
    }
}
