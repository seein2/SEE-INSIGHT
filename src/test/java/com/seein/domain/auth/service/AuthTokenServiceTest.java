package com.seein.domain.auth.service;

import com.seein.domain.auth.dto.AuthTokens;
import com.seein.domain.auth.dto.RefreshTokenSession;
import com.seein.domain.auth.repository.RefreshTokenRepository;
import com.seein.global.security.jwt.JwtProperties;
import com.seein.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @InjectMocks
    private AuthTokenService authTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenHashService refreshTokenHashService;

    @Test
    @DisplayName("이메일 기준으로 액세스 토큰과 리프레시 토큰을 발급한다")
    void issueTokens_success() {
        // given
        given(jwtTokenProvider.createAccessToken(1, "test@example.com")).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(eq(1), eq("test@example.com"), anyString())).willReturn("refresh-token");
        given(jwtProperties.getRefreshExpiration()).willReturn(604800000L);
        given(refreshTokenHashService.hash("refresh-token")).willReturn("hashed-refresh-token");

        // when
        AuthTokens authTokens = authTokenService.issueTokens(1, "test@example.com");

        // then
        assertThat(authTokens.getAccessToken()).isEqualTo("access-token");
        assertThat(authTokens.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(authTokens.getRefreshTokenId()).isNotBlank();

        ArgumentCaptor<RefreshTokenSession> captor = ArgumentCaptor.forClass(RefreshTokenSession.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(1);
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-refresh-token");
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 회전 발급한다")
    void refresh_success() {
        // given
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        RefreshTokenSession session = RefreshTokenSession.of(1, "old-jti", "hashed-old-token", LocalDateTime.now().plusDays(7));

        given(jwtTokenProvider.getValidatedRefreshClaims("refresh-token")).willReturn(claims);
        given(claims.get("member_id", Integer.class)).willReturn(1);
        given(claims.getId()).willReturn("old-jti");
        given(claims.getSubject()).willReturn("test@example.com");
        given(refreshTokenRepository.find(1, "old-jti")).willReturn(Optional.of(session));
        given(refreshTokenHashService.hash("refresh-token")).willReturn("hashed-old-token");
        given(jwtTokenProvider.createAccessToken(1, "test@example.com")).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(eq(1), eq("test@example.com"), anyString())).willReturn("new-refresh-token");
        given(jwtProperties.getRefreshExpiration()).willReturn(604800000L);
        given(refreshTokenHashService.hash("new-refresh-token")).willReturn("hashed-new-token");

        // when
        AuthTokens response = authTokenService.refresh("refresh-token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository).delete(session);
        verify(refreshTokenRepository).save(org.mockito.ArgumentMatchers.any(RefreshTokenSession.class));
    }
}
