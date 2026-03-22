package com.seein.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.seein.domain.dashboard.service.DashboardService;

import java.util.Collections;

/**
 * 시작 페이지 컨트롤러
 * 소셜 로그인 진입 화면을 제공
 */
@Tag(name = "Home", description = "메인 페이지 API")
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;

    /**
     * 루트 시작 페이지
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("loginPageUrl", "/login");
        model.addAttribute("profileUrl", "/me");

        try {
            var dashboard = dashboardService.getDashboard();
            model.addAttribute("bestKeywords", dashboard.getBestKeywords());
        } catch (Exception e) {
            model.addAttribute("bestKeywords", Collections.emptyList());
        }

        return "home";
    }

    /**
     * 로그인/회원가입 페이지
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleLoginUrl", "/oauth2/authorization/google");
        model.addAttribute("naverLoginUrl", "/oauth2/authorization/naver");
        return "login";
    }
}
