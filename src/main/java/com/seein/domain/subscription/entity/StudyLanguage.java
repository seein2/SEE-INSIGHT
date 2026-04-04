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

    /**
     * 라벨 조회
     */
    public String getLabel() {
        return label;
    }

    /**
     * 검색 언어 코드 조회
     */
    public String getSearchLanguageCode() {
        return searchLanguageCode;
    }
}
