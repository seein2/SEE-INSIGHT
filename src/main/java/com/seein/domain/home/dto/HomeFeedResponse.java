package com.seein.domain.home.dto;

import com.seein.domain.content.dto.LearningContentCardResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 홈 학습 피드 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class HomeFeedResponse {

    private final String selectedStudyLanguage;
    private final String selectedStudyLanguageLabel;
    private final String selectedLearningStyle;
    private final String selectedLearningStyleLabel;
    private final LearningContentCardResponse featuredContent;
    private final List<LearningContentCardResponse> feedCards;
}
