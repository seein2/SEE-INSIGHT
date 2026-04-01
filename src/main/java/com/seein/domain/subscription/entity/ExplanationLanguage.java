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

    public String getLabel() {
        return label;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
