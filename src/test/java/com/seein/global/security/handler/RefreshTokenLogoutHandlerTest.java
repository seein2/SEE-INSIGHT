package com.seein.global.security.handler;

import com.seein.domain.auth.service.AuthTokenService;
import com.seein.global.security.cookie.AuthCookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenLogoutHandlerTest {

    @InjectMocks
    private RefreshTokenLogoutHandler refreshTokenLogoutHandler;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("로그아웃 시 refresh_token 쿠키를 무효화한다")
    void logout_success() {
        // given
        given(request.getCookies()).willReturn(new Cookie[]{
                new Cookie(AuthCookieService.REFRESH_TOKEN_COOKIE_NAME, "refresh-token")
        });

        // when
        refreshTokenLogoutHandler.logout(request, response, null);

        // then
        verify(authTokenService).invalidateRefreshToken("refresh-token");
    }
}
