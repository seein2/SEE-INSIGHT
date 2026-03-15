package com.seein.domain.auth.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 시작 페이지 컨트롤러
 * 소셜 로그인 진입 화면을 제공
 */
@Controller
public class HomeController {

    /**
     * 루트 시작 페이지
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("googleLoginUrl", "/oauth2/authorization/google");
        model.addAttribute("naverLoginUrl", "/oauth2/authorization/naver");
        model.addAttribute("profileUrl", "/api/v1/users/me");
        return "home";
    }
}
