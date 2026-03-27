package com.seein.global.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn("0123456789012345678901234567890101234567890123456789012345678901");
        lenient().when(jwtProperties.getAccessExpiration()).thenReturn(3600000L);
        lenient().when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        jwtTokenProvider = new JwtTokenProvider(jwtProperties, customUserDetailsService);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("액세스 토큰은 access 타입으로 검증된다")
    void validateAccessToken_success() {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(1, "test@example.com");

        // when & then
        assertThat(jwtTokenProvider.validateAccessToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateRefreshToken(accessToken)).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰은 refresh 타입과 jti를 가진다")
    void createRefreshToken_success() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(1, "test@example.com", "refresh-jti");

        // when & then
        assertThat(jwtTokenProvider.validateRefreshToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.validateAccessToken(refreshToken)).isFalse();
        assertThat(jwtTokenProvider.getMemberIdFromToken(refreshToken)).isEqualTo(1);
        assertThat(jwtTokenProvider.getTokenId(refreshToken)).isEqualTo("refresh-jti");
    }
}
