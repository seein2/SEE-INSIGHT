package com.seein.domain.member.controller;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.service.MemberService;
import com.seein.global.security.jwt.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 회원 페이지 컨트롤러
 * 로그인 사용자 화면을 제공한다.
 */
@Controller
@RequiredArgsConstructor
public class MemberPageController {

    private final MemberService memberService;

    /**
     * 내 정보 페이지
     */
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        MemberResponse member = memberService.getMember(principal.getMemberId());
        model.addAttribute("member", member);
        return "member/profile";
    }
}
