package com.seein.domain.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 학습 구독 미리보기 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SubscriptionPreviewRequest {

    @NotNull(message = "학습 언어는 필수입니다.")
    private StudyLanguage studyLanguage;

    @NotNull(message = "해설 언어는 필수입니다.")
    private ExplanationLanguage explanationLanguage;

    @NotNull(message = "학습 스타일은 필수입니다.")
    private LearningStyle learningStyle;

    @NotNull(message = "난이도는 필수입니다.")
    private DifficultyLevel difficultyLevel;

    @NotNull(message = "수신 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime deliveryTime;
}
