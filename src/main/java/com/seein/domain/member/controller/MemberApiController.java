package com.seein.domain.member.controller;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.MemberUpdateRequest;
import com.seein.domain.member.service.MemberService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 API 컨트롤러
 * 회원 정보 변경 기능 제공
 */
@Tag(name = "Members", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 내 회원 정보 수정
     */
    @Operation(summary = "내 회원 정보 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다.")
    @PatchMapping("/me/nickname")
    public GlobalResponseDto<MemberResponse> updateMyMember(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        memberService.updateMemberInfo(principal.getMemberId(), request.getNickname());
        return GlobalResponseDto.success(memberService.getMember(principal.getMemberId()));
    }
}
