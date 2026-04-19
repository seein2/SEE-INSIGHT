package com.seein.domain.subscription.entity;

/**
 * 학습 스타일 Enum
 */
public enum LearningStyle {
    NEWS_READING("뉴스 읽기", "현지 뉴스와 짧은 기사 중심"),
    DAILY_CONVERSATION("일상 회화", "레거시 설정: 신규 구독 선택에서는 숨김"),
    TODAYS_EXPRESSION("오늘의 표현", "오래 쓰이는 표현과 실제 문맥 중심"),
    BALANCED("균형 학습", "읽기·표현·복습을 부담 없이 연결");

    private final String label;
    private final String description;

    LearningStyle(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * 라벨 조회
     */
    public String getLabel() {
        return label;
    }

    /**
     * 설명 조회
     */
    public String getDescription() {
        return description;
    }

    /**
     * 신규 사용자에게 노출할 학습 스타일 여부
     */
    public boolean isSelectable() {
        return this != DAILY_CONVERSATION;
    }
}
