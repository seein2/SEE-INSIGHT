package com.seein.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Perplexity API 설정 프로퍼티
 * application-local.yaml의 perplexity 섹션과 바인딩
 */
@Getter
@Component
@ConfigurationProperties(prefix = "perplexity")
public class PerplexityProperties {

    private String apiKey;
    private String baseUrl;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
