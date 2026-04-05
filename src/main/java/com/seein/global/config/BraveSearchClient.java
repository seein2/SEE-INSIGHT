package com.seein.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Brave Search API 클라이언트
 */
@Slf4j
@Component
public class BraveSearchClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * Brave Search 클라이언트 생성
     */
    public BraveSearchClient(BraveProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeaders(headers -> {
                    headers.set("X-Subscription-Token", properties.getApiKey());
                    if (StringUtils.hasText(properties.getApiVersion())) {
                        headers.set("Api-Version", properties.getApiVersion());
                    }
                })
                .build();
    }

    /**
     * Brave Web Search 조회
     */
    public List<SearchResult> searchWeb(String query, String searchLanguageCode, String freshness) {
        try {
            String responseJson = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/res/v1/web/search")
                            .queryParam("q", query)
                            .queryParam("search_lang", searchLanguageCode)
                            .queryParam("count", 5)
                            .queryParam("freshness", freshness)
                            .build())
                    .retrieve()
                    .body(String.class);

            return extractSearchResults(responseJson);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Brave Search API 호출 실패 - query={}, searchLanguageCode={}, freshness={}, error={}",
                    query, searchLanguageCode, freshness, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave Search API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Web Search 응답에서 결과 목록 추출
     */
    private List<SearchResult> extractSearchResults(String responseJson) {
        if (!StringUtils.hasText(responseJson)) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave Search API 응답이 비어 있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode resultsNode = root.path("web").path("results");
            List<SearchResult> results = new ArrayList<>();

            if (!resultsNode.isArray()) {
                return results;
            }

            for (JsonNode resultNode : resultsNode) {
                results.add(new SearchResult(
                        textOrNull(resultNode, "title"),
                        textOrNull(resultNode, "description"),
                        textOrNull(resultNode, "url")
                ));
            }
            return results;
        } catch (Exception e) {
            log.error("Brave Search API 응답 파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave Search API 응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * JsonNode 문자열 값 추출
     */
    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() || fieldNode.isNull() ? null : fieldNode.asText();
    }

    public record SearchResult(String title, String description, String url) {
    }
}
