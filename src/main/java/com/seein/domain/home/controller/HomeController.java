package com.seein.domain.home.controller;

import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.home.service.HomeFeedService;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 홈 페이지 컨트롤러
 */
@Tag(name = "Home", description = "학습 피드 홈")
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeFeedService homeFeedService;

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
        model.addAttribute("learningStyles", LearningStyle.values());
        model.addAttribute("loginPageUrl", "/login");
        model.addAttribute("profileUrl", "/me");
        model.addAttribute("subscriptionsUrl", "/subscriptions");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleLoginUrl", "/oauth2/authorization/google");
        model.addAttribute("naverLoginUrl", "/oauth2/authorization/naver");
        return "login";
    }
}
