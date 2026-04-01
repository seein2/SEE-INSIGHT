package com.seein.domain.subscription.controller;

import com.seein.domain.member.dto.MyPageResponse;
import com.seein.domain.member.service.MemberService;
import com.seein.domain.subscription.service.SubscriptionService;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.security.jwt.MemberPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

/**
 * 학습 구독 페이지 컨트롤러
 */
@Tag(name = "Subscriptions", description = "키워드 구독 페이지")
@Controller
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPageController {

    private final SubscriptionService subscriptionService;
    private final MemberService memberService;

    /**
     * 구독 설정 wizard 페이지
     */
    @GetMapping
    public String subscriptions(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(required = false) StudyLanguage studyLanguage,
            @RequestParam(required = false) LearningStyle learningStyle,
            @RequestParam(required = false) Integer editSubscriptionId,
            Model model
    ) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(principal.getMemberId());
        MyPageResponse myPage = memberService.getMyPage(principal.getMemberId());

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("myPage", myPage);
        model.addAttribute("studyLanguages", StudyLanguage.values());
        model.addAttribute("explanationLanguages", ExplanationLanguage.values());
        model.addAttribute("learningStyles", LearningStyle.values());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("prefillStudyLanguage", studyLanguage != null ? studyLanguage : StudyLanguage.ENGLISH);
        model.addAttribute("prefillExplanationLanguage", ExplanationLanguage.KOREAN);
        model.addAttribute("prefillLearningStyle", learningStyle != null ? learningStyle : LearningStyle.BALANCED);
        model.addAttribute("prefillDifficultyLevel", DifficultyLevel.BEGINNER);
        model.addAttribute("prefillDeliveryTime", LocalTime.of(8, 0));
        model.addAttribute("editSubscriptionId", editSubscriptionId);
        return "subscriptions/wizard";
    }

    /**
     * 구독 완료 페이지
     */
    @GetMapping("/completed/{subscriptionId}")
    public String completed(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Integer subscriptionId,
            Model model
    ) {
        SubscriptionResponse subscription = subscriptionService.getSubscription(principal.getMemberId(), subscriptionId);
        model.addAttribute("subscription", subscription);
        return "subscriptions/completed";
    }
}
