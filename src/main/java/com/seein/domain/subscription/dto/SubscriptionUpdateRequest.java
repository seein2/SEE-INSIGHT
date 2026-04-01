package com.seein.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 구독 설정 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SubscriptionUpdateRequest {

    private StudyLanguage studyLanguage;
    private ExplanationLanguage explanationLanguage;
    private LearningStyle learningStyle;
    private DifficultyLevel difficultyLevel;

    private Boolean isActive;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime deliveryTime;
}
