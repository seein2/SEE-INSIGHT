package com.seein.domain.auth.service;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.dto.TokenResponse;
import com.seein.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @InjectMocks
    private AuthTokenService authTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("이메일 기준으로 액세스 토큰과 리프레시 토큰을 발급한다")
    void issueTokens_success() {
        // given
        given(jwtTokenProvider.createAccessToken("test@example.com")).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken("test@example.com")).willReturn("refresh-token");

        // when
        AuthTokens authTokens = authTokenService.issueTokens("test@example.com");

        // then
        assertThat(authTokens.getAccessToken()).isEqualTo("access-token");
        assertThat(authTokens.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급한다")
    void refresh_success() {
        // given
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken("refresh-token")).willReturn("test@example.com");
        given(jwtTokenProvider.createAccessToken("test@example.com")).willReturn("new-access-token");

        // when
        TokenResponse response = authTokenService.refresh("Bearer refresh-token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }
}
