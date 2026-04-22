package com.seein.domain.home.controller;

import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.home.service.HomeFeedService;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.security.oauth2.OAuth2LoginError;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

/**
 * 홈 페이지 컨트롤러
 */
@Hidden
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeFeedService homeFeedService;

    /**
     * 홈 페이지 렌더링
     */
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) StudyLanguage studyLanguage,
            @RequestParam(required = false) LearningStyle learningStyle,
            Model model,
            Authentication authentication
    ) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        HomeFeedResponse feed = homeFeedService.getHomeFeed(studyLanguage, learningStyle);

        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("feed", feed);
        model.addAttribute("studyLanguages", StudyLanguage.values());
        model.addAttribute("learningStyles", Arrays.stream(LearningStyle.values())
                .filter(LearningStyle::isSelectable)
                .toList());
        model.addAttribute("loginPageUrl", "/login");
        model.addAttribute("profileUrl", "/me");
        model.addAttribute("subscriptionsUrl", "/subscriptions");
        return "home";
    }

    /**
     * 로그인 페이지 렌더링
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String oauthError, Model model) {
        model.addAttribute("googleLoginUrl", "/oauth2/authorization/google");
        model.addAttribute("naverLoginUrl", "/oauth2/authorization/naver");
        OAuth2LoginError.fromCode(oauthError)
                .ifPresent(error -> model.addAttribute("oauthErrorMessage", error.getMessage()));
        return "login";
    }
}
