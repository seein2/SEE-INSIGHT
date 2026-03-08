package com.seein.domain.auth.controller;

import com.seein.domain.auth.dto.TokenResponse;
import com.seein.domain.member.service.MemberService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.CustomUserDetails;
import com.seein.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * 로그인, 로그아웃, 토큰 재발급, 회원 탈퇴 등 인증 관련 API 제공
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    /**
     * OAuth2 로그인 페이지로 리다이렉트
     * 실제 로그인은 /oauth2/authorization/google 로 처리됨
     */
    @Operation(summary = "Google OAuth2 로그인", description = "Google OAuth2 로그인 페이지로 리다이렉트")
    @ApiResponse(responseCode = "200", description = "로그인 안내 메시지 반환")
    @GetMapping("/login")
    public GlobalResponseDto<String> login() {
        return GlobalResponseDto.success("OAuth2 로그인은 /oauth2/authorization/google 으로 접속하세요.");
    }

    /**
     * 로그아웃
     * 클라이언트에서 토큰 삭제 처리
     */
    @Operation(summary = "로그아웃", description = "로그아웃 처리 (클라이언트에서 토큰 삭제)")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public GlobalResponseDto<String> logout() {
        return GlobalResponseDto.success("로그아웃되었습니다.");
    }

    /**
     * Access Token 재발급
     * Refresh Token을 검증하여 새로운 Access Token 발급
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token 발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public GlobalResponseDto<TokenResponse> refresh(
            @Parameter(description = "Bearer {Refresh Token}", required = true)
            @RequestHeader("Authorization") String refreshToken) {
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public GlobalResponseDto<CustomUserDetails> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return GlobalResponseDto.success(userDetails);
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    @Operation(summary = "회원 탈퇴", description = "사용자의 계정을 비활성화 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @DeleteMapping("/withdraw")
    public GlobalResponseDto<String> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.withdraw(userDetails.getMemberId());
        return GlobalResponseDto.success("회원 탈퇴 완료");
    }
}
