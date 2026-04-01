package com.seein.domain.subscription.entity;

/**
 * 난이도 Enum
 */
public enum DifficultyLevel {
    BEGINNER("초급", "짧고 쉬운 문장"),
    INTERMEDIATE("중급", "실전 문장과 기본 어휘"),
    ADVANCED("고급", "자연스러운 원문 중심");

    private final String label;
    private final String description;

    DifficultyLevel(String label, String description) {
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
