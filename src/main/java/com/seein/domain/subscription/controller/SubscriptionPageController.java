package com.seein.domain.subscription.controller;

import com.seein.domain.subscription.service.SubscriptionService;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.global.security.jwt.MemberPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 키워드 구독 페이지 컨트롤러
 * 키워드 구독 목록을 조회하여 Thymeleaf 템플릿에 전달하는 역할
 */
@Tag(name = "Subscriptions", description = "키워드 구독 페이지")
@Controller
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPageController {

    private final SubscriptionService subscriptionService;

    /**
     * 내 구독 목록 조회
     */
    @GetMapping
    public String keywords(@AuthenticationPrincipal MemberPrincipal principal, @PageableDefault(size = 5) Pageable pageable, Model model) {
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(principal.getMemberId(), pageable);

        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions/list";
    }
}
