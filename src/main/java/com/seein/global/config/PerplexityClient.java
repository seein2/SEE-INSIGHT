package com.seein.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Perplexity API 클라이언트
 * 학습 피드와 이메일 학습용 콘텐츠 생성을 담당한다.
 */
@Slf4j
@Component
public class PerplexityClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PerplexityClient(PerplexityProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 학습 콘텐츠 생성
     */
    public String generateLearningContent(String systemPrompt, String userPrompt, String searchLanguageCode) {
        Map<String, Object> requestBody = Map.of(
                "model", "sonar",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 1200,
                "search_recency_filter", "day",
                "search_language_filter", List.of(searchLanguageCode),
                "language_preference", searchLanguageCode
        );

        try {
            return restClient.post()
                    .uri("/v1/sonar")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Perplexity API 호출 실패 - searchLanguageCode: {}, error: {}", searchLanguageCode, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Perplexity API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답 JSON에서 content 텍스트 추출
     * 경로: choices[0].message.content
     */
    public String extractContentFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Perplexity API 응답 파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "API 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답 JSON에서 첫 번째 citation URL 추출
     */
    public String extractFirstCitation(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode citations = root.path("citations");
            if (citations.isArray() && !citations.isEmpty()) {
                return citations.get(0).asText();
            }
            return null;
        } catch (Exception e) {
            log.warn("Citation 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}
