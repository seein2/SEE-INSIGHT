package com.seein.global.security.jwt;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.service.AuthTokenService;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import com.seein.global.security.cookie.AuthCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private AuthCookieService authCookieService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Access Token이 없고 Refresh Token이 유효하면 자동 재발급 후 인증한다")
    void doFilterInternal_autoRefresh_success() throws Exception {
        // given
        AuthTokens authTokens = AuthTokens.of("new-access-token", "new-refresh-token", "refresh-jti");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user", null);

        given(request.getRequestURI()).willReturn("/subscriptions");
        given(request.getCookies()).willReturn(new Cookie[]{
                new Cookie(AuthCookieService.REFRESH_TOKEN_COOKIE_NAME, "refresh-token")
        });
        given(request.isSecure()).willReturn(false);
        given(authTokenService.refresh("refresh-token")).willReturn(authTokens);
        given(jwtTokenProvider.getAuthentication("new-access-token")).willReturn(authentication);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(authTokenService).refresh("refresh-token");
        verify(authCookieService).addAuthCookies(response, authTokens, false);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    @DisplayName("Refresh Token 재발급에 실패하면 쿠키를 제거하고 인증하지 않는다")
    void doFilterInternal_autoRefresh_fail() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/subscriptions");
        given(request.getCookies()).willReturn(new Cookie[]{
                new Cookie(AuthCookieService.REFRESH_TOKEN_COOKIE_NAME, "refresh-token")
        });
        given(request.isSecure()).willReturn(false);
        given(authTokenService.refresh("refresh-token")).willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        verify(authTokenService).invalidateRefreshToken("refresh-token");
        verify(authCookieService).clearAuthCookies(response, false);
        verify(jwtTokenProvider, never()).getAuthentication("new-access-token");
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
