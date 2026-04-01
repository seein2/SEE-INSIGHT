package com.seein.domain.subscription.dto;

import com.seein.domain.content.dto.LearningContentCardResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학습 구독 미리보기 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class SubscriptionPreviewResponse {

    private final String studyLanguage;
    private final String studyLanguageLabel;
    private final String explanationLanguage;
    private final String explanationLanguageLabel;
    private final String learningStyle;
    private final String learningStyleLabel;
    private final String difficultyLevel;
    private final String difficultyLevelLabel;
    private final String deliveryTime;
    private final String scheduleLabel;
    private final LearningContentCardResponse previewContent;
}
