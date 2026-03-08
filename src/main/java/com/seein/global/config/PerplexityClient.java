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
 * Sonar 모델을 사용하여 키워드 기반 뉴스 요약 생성
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
     * 키워드 기반 뉴스 요약 생성
     *
     * @param keyword 검색할 키워드
     * @return AI가 생성한 뉴스 요약 텍스트
     */
    public String generateNewsSummary(String keyword) {
        String systemPrompt = "당신은 한국어 뉴스 요약 전문가입니다. "
                + "주어진 키워드에 대한 오늘의 최신 뉴스를 검색하여 핵심 내용을 3~5문장으로 요약해주세요. "
                + "출처가 있다면 함께 제공해주세요.";

        String userPrompt = "'" + keyword + "' 키워드에 대한 오늘의 최신 뉴스를 요약해주세요.";

        Map<String, Object> requestBody = Map.of(
                "model", "sonar",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2,
                "max_tokens", 1024
        );

        try {
            String responseJson = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractContent(responseJson);
        } catch (Exception e) {
            log.error("Perplexity API 호출 실패 - keyword: {}, error: {}", keyword, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "Perplexity API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Perplexity API 응답에서 citations(출처 URL) 추출
     *
     * @param keyword 검색할 키워드
     * @return 첫 번째 출처 URL (없으면 null)
     */
    public String generateNewsWithSource(String keyword) {
        String systemPrompt = "당신은 한국어 뉴스 요약 전문가입니다. "
                + "주어진 키워드에 대한 오늘의 최신 뉴스를 검색하여 핵심 내용을 3~5문장으로 요약해주세요.";

        String userPrompt = "'" + keyword + "' 키워드에 대한 오늘의 최신 뉴스를 요약해주세요.";

        Map<String, Object> requestBody = Map.of(
                "model", "sonar",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2,
                "max_tokens", 1024
        );

        try {
            String responseJson = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return responseJson;
        } catch (Exception e) {
            log.error("Perplexity API 호출 실패 - keyword: {}, error: {}", keyword, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "Perplexity API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답 JSON에서 content 텍스트 추출
     * 경로: choices[0].message.content
     */
    private String extractContent(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Perplexity API 응답 파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "API 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답 JSON에서 첫 번째 citation URL 추출
     * 경로: citations[0]
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

    /**
     * API 응답 JSON에서 content 텍스트 추출 (public)
     */
    public String extractContentFromResponse(String responseJson) {
        return extractContent(responseJson);
    }
}
