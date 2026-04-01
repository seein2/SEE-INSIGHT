package com.seein.domain.subscription.entity;

/**
 * 학습 언어 Enum
 */
public enum StudyLanguage {
    ENGLISH("영어", "en"),
    JAPANESE("일본어", "ja"),
    CHINESE("중국어", "zh");

    private final String label;
    private final String searchLanguageCode;

    StudyLanguage(String label, String searchLanguageCode) {
        this.label = label;
        this.searchLanguageCode = searchLanguageCode;
    }

    public String getLabel() {
        return label;
    }

    public String getSearchLanguageCode() {
        return searchLanguageCode;
    }
}
