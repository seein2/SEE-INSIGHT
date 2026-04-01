package com.seein.domain.subscription.entity;

/**
 * 학습 스타일 Enum
 */
public enum LearningStyle {
    PRACTICAL_READING("실전 읽기", "짧은 기사와 실제 문장 중심"),
    DAILY_CONVERSATION("일상 회화", "바로 써먹는 회화 표현 중심"),
    TODAYS_EXPRESSION("오늘의 표현", "관용구와 핵심 표현 중심"),
    BALANCED("균형형", "읽기·회화·표현을 고르게 학습");

    private final String label;
    private final String description;

    LearningStyle(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
