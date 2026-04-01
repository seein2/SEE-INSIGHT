package com.seein.domain.home.controller;

import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.home.service.HomeFeedService;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.dto.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학습 피드 API 컨트롤러
 */
@Tag(name = "Feed", description = "학습 피드 API")
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final HomeFeedService homeFeedService;

    @Operation(summary = "학습 피드 조회", description = "언어와 스타일 기준으로 학습 피드를 조회합니다.")
    @GetMapping
    public GlobalResponseDto<HomeFeedResponse> getFeed(
            @RequestParam(required = false) StudyLanguage studyLanguage,
            @RequestParam(required = false) LearningStyle learningStyle
    ) {
        return GlobalResponseDto.success(homeFeedService.getHomeFeed(studyLanguage, learningStyle));
    }
}
