package com.seein.domain.subscription.entity;

/**
 * 해설 언어 Enum
 */
public enum ExplanationLanguage {
    KOREAN("한국어", "ko"),
    ENGLISH("영어", "en");

    private final String label;
    private final String languageCode;

    ExplanationLanguage(String label, String languageCode) {
        this.label = label;
        this.languageCode = languageCode;
    }

    /**
     * 라벨 조회
     */
    public String getLabel() {
        return label;
    }

    /**
     * 언어 코드 조회
     */
    public String getLanguageCode() {
        return languageCode;
    }
}
