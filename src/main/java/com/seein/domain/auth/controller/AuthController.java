package com.seein.domain.auth.controller;

import com.seein.domain.auth.dto.TokenResponse;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.CustomUserDetails;
import com.seein.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * 로그인, 로그아웃, 토큰 재발급 등 인증 관련 API 제공
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * OAuth2 로그인 페이지로 리다이렉트
     * 실제 로그인은 /oauth2/authorization/google 로 처리됨
     */
    @Operation(summary = "Google OAuth2 로그인", description = "Google OAuth2 로그인 페이지로 리다이렉트")
    @GetMapping("/login")
    public GlobalResponseDto<String> login() {
        return GlobalResponseDto.success("OAuth2 로그인은 /oauth2/authorization/google 으로 접속하세요.");
    }

    /**
     * 로그아웃
     * 클라이언트에서 토큰 삭제 처리
     */
    @Operation(summary = "로그아웃", description = "로그아웃 처리 (클라이언트에서 토큰 삭제)")
    @PostMapping("/logout")
    public GlobalResponseDto<String> logout() {
        return GlobalResponseDto.success("로그아웃되었습니다.");
    }

    /**
     * Access Token 재발급
     * Refresh Token을 검증하여 새로운 Access Token 발급
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token 발급")
    @PostMapping("/refresh")
    public GlobalResponseDto<TokenResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        // Bearer 접두사 제거
        String token = refreshToken.replace("Bearer ", "");

        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 새로운 Access Token 발급
        String email = jwtTokenProvider.getEmailFromToken(token);
        String newAccessToken = jwtTokenProvider.createAccessToken(email);

        TokenResponse response = TokenResponse.of(newAccessToken, token);
        return GlobalResponseDto.success(response);
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보 조회")
    @GetMapping("/me")
    public GlobalResponseDto<CustomUserDetails> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponseDto.success(userDetails);
    }
}
