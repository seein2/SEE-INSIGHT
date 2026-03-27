package com.seein.domain.auth.controller;

import com.seein.domain.auth.service.AuthTokenService;
import com.seein.domain.member.service.MemberService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.cookie.AuthCookieService;
import com.seein.global.security.jwt.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;
    private final MemberService memberService;

    /**
     * Access Token 재발급
     * Refresh Token을 검증하여 새로운 Access Token 발급
     */
    @Operation(summary = "토큰 재발급", description = "refresh_token 쿠키를 사용하여 Access Token과 Refresh Token을 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public GlobalResponseDto<String> refresh(@CookieValue(name = AuthCookieService.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        authCookieService.addAuthCookies(response, authTokenService.refresh(refreshToken), request.isSecure());
        return GlobalResponseDto.success("토큰이 재발급되었습니다.");
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
    public GlobalResponseDto<String> withdraw(@AuthenticationPrincipal MemberPrincipal userDetails) {
        memberService.withdraw(userDetails.getMemberId());
        return GlobalResponseDto.success("회원 탈퇴 완료");
    }
}
