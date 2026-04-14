package com.seein.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Brave Search API 설정 프로퍼티
 * application-local.yaml의 brave 섹션과 바인딩
 */
@Getter
@Component
@ConfigurationProperties(prefix = "brave")
public class BraveProperties {

    private String apiKey;
    private String baseUrl;

    /**
     * API 키 설정
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 기본 URL 설정
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
