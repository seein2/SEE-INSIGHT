package com.seein.domain.member.controller;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.NicknameUpdateRequest;
import com.seein.domain.member.service.MemberService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자 API 컨트롤러
 * 내 정보 조회, 닉네임 변경 기능 제공
 */
@Tag(name = "Users", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 내 정보 조회
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public GlobalResponseDto<MemberResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberResponse response = memberService.getMember(userDetails.getMemberId());
        return GlobalResponseDto.success(response);
    }

    /**
     * 닉네임 변경
     */
    @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 변경합니다.")
    @PatchMapping("/me/nickname")
    public GlobalResponseDto<Map<String, String>> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NicknameUpdateRequest request) {
        String nickname = memberService.updateNickname(userDetails.getMemberId(), request.getNickname());
        return GlobalResponseDto.success(Map.of("nickname", nickname));
    }
}
