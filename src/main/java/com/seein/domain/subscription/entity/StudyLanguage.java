package com.seein.domain.subscription.entity;

/**
 * 학습 언어 Enum
 */
public enum StudyLanguage {
    ENGLISH("영어", "en", "en", "US", "en-US"),
    JAPANESE("일본어", "jp", "ja", "JP", "ja-JP"),
    CHINESE("중국어", "zh-hans", "zh", "CN", "zh-CN");

    private final String label;
    private final String searchLanguageCode;
    private final String contentLanguageCode;
    private final String searchCountryCode;
    private final String uiLanguageCode;

    StudyLanguage(
            String label,
            String searchLanguageCode,
            String contentLanguageCode,
            String searchCountryCode,
            String uiLanguageCode
    ) {
        this.label = label;
        this.searchLanguageCode = searchLanguageCode;
        this.contentLanguageCode = contentLanguageCode;
        this.searchCountryCode = searchCountryCode;
        this.uiLanguageCode = uiLanguageCode;
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

    /**
     * 콘텐츠 언어 코드 조회
     */
    public String getContentLanguageCode() {
        return contentLanguageCode;
    }

    /**
     * Brave 검색 국가 코드 조회
     */
    public String getSearchCountryCode() {
        return searchCountryCode;
    }

    /**
     * Brave UI 언어 코드 조회
     */
    public String getUiLanguageCode() {
        return uiLanguageCode;
    }
}
