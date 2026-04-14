package com.seein.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.seein.domain.content.entity.ContentSourceType;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

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
                })
                .build();
    }

    /**
     * Brave Web Search 조회
     */
    public List<SearchResult> searchWeb(String query, String searchLanguageCode, String freshness) {
        return searchWeb(new SearchRequest(
                query,
                null,
                searchLanguageCode,
                null,
                freshness,
                5,
                false,
                false
        ));
    }

    /**
     * Brave Web Search 조회
     */
    public List<SearchResult> searchWeb(SearchRequest request) {
        return search("/res/v1/web/search", ContentSourceType.WEB, request);
    }

    /**
     * Brave News Search 조회
     */
    public List<SearchResult> searchNews(SearchRequest request) {
        return search("/res/v1/news/search", ContentSourceType.NEWS, request);
    }

    /**
     * Brave LLM Context 조회
     */
    public List<LlmContextResult> fetchLlmContext(LlmContextRequest request) {
        try {
            String responseJson = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/res/v1/llm/context")
                            .queryParam("q", request.query())
                            .queryParam("count", request.count())
                            .queryParam("maximum_number_of_urls", request.maximumNumberOfUrls())
                            .queryParam("maximum_number_of_tokens", request.maximumNumberOfTokens())
                            .queryParam("context_threshold_mode", request.contextThresholdMode())
                            .queryParamIfPresent("country", java.util.Optional.ofNullable(request.country()))
                            .queryParamIfPresent("search_lang", java.util.Optional.ofNullable(request.searchLanguage()))
                            .queryParamIfPresent("freshness", java.util.Optional.ofNullable(request.freshness()))
                            .build())
                    .retrieve()
                    .body(String.class);

            return extractLlmContextResults(responseJson);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Brave LLM Context API 호출 실패 - query={}, error={}", request.query(), e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave LLM Context API 호출 실패: " + e.getMessage());
        }
    }

    private List<SearchResult> search(String path, ContentSourceType sourceType, SearchRequest request) {
        try {
            String responseJson = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("q", request.query())
                            .queryParam("count", request.count())
                            .queryParam("text_decorations", false)
                            .queryParam("extra_snippets", request.extraSnippets())
                            .queryParam("include_fetch_metadata", request.includeFetchMetadata())
                            .queryParamIfPresent("country", java.util.Optional.ofNullable(request.country()))
                            .queryParamIfPresent("search_lang", java.util.Optional.ofNullable(request.searchLanguage()))
                            .queryParamIfPresent("ui_lang", java.util.Optional.ofNullable(request.uiLanguage()))
                            .queryParamIfPresent("freshness", java.util.Optional.ofNullable(request.freshness()))
                            .build())
                    .retrieve()
                    .body(String.class);

            return extractSearchResults(responseJson, sourceType);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Brave Search API 호출 실패 - path={}, query={}, searchLanguageCode={}, freshness={}, error={}",
                    path, request.query(), request.searchLanguage(), request.freshness(), e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave Search API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Web Search 응답에서 결과 목록 추출
     */
    List<SearchResult> extractSearchResults(String responseJson, ContentSourceType sourceType) {
        if (!StringUtils.hasText(responseJson)) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "Brave Search API 응답이 비어 있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode resultsNode = sourceType == ContentSourceType.NEWS
                    ? firstArray(root.path("results"), root.path("news").path("results"))
                    : firstArray(root.path("web").path("results"), root.path("results"));
            List<SearchResult> results = new ArrayList<>();

            if (!resultsNode.isArray()) {
                return results;
            }

            for (JsonNode resultNode : resultsNode) {
                JsonNode metaUrl = resultNode.path("meta_url");
                JsonNode profile = resultNode.path("profile");
                JsonNode thumbnail = resultNode.path("thumbnail");
                results.add(new SearchResult(
                        textOrNull(resultNode, "title"),
                        textOrNull(resultNode, "description"),
                        textOrNull(resultNode, "url"),
                        textArray(resultNode.path("extra_snippets")),
                        firstText(resultNode, "page_age", "age"),
                        textOrNull(resultNode, "language"),
                        firstText(profile, "name", "long_name"),
                        firstText(metaUrl, "hostname", "netloc"),
                        textOrNull(resultNode, "content_type"),
                        textOrNull(thumbnail, "src"),
                        booleanOrNull(resultNode, "breaking"),
                        firstText(resultNode, "fetched_at", "fetch_time", "age"),
                        sourceType
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

    List<LlmContextResult> extractLlmContextResults(String responseJson) {
        if (!StringUtils.hasText(responseJson)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode genericNode = root.path("grounding").path("generic");
            JsonNode sourcesNode = root.path("sources");
            List<LlmContextResult> results = new ArrayList<>();

            if (!genericNode.isArray()) {
                return results;
            }

            for (JsonNode item : genericNode) {
                String url = textOrNull(item, "url");
                JsonNode source = StringUtils.hasText(url) ? sourcesNode.path(url) : MissingNode.getInstance();
                results.add(new LlmContextResult(
                        url,
                        firstText(item, "title"),
                        textArray(item.path("snippets")),
                        firstText(source, "hostname"),
                        source.path("age").isMissingNode() || source.path("age").isNull()
                                ? null
                                : source.path("age").toString()
                ));
            }
            return results;
        } catch (Exception e) {
            log.warn("Brave LLM Context 응답 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private JsonNode firstArray(JsonNode first, JsonNode second) {
        return first.isArray() ? first : second;
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String text = textOrNull(node, fieldName);
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return null;
    }

    private List<String> textArray(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(node.spliterator(), false)
                .map(value -> value.isTextual() ? value.asText() : value.toString())
                .filter(StringUtils::hasText)
                .toList();
    }

    private Boolean booleanOrNull(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isBoolean() ? fieldNode.asBoolean() : null;
    }

    public record SearchRequest(
            String query,
            String country,
            String searchLanguage,
            String uiLanguage,
            String freshness,
            int count,
            boolean extraSnippets,
            boolean includeFetchMetadata
    ) {
    }

    public record LlmContextRequest(
            String query,
            String country,
            String searchLanguage,
            String freshness,
            int count,
            int maximumNumberOfUrls,
            int maximumNumberOfTokens,
            String contextThresholdMode
    ) {
    }

    public record SearchResult(
            String title,
            String description,
            String url,
            List<String> extraSnippets,
            String pageAge,
            String language,
            String sourceName,
            String sourceHost,
            String contentType,
            String thumbnailUrl,
            Boolean breaking,
            String fetchedAt,
            ContentSourceType sourceType
    ) {

        public SearchResult(String title, String description, String url) {
            this(title, description, url, List.of(), null, null, null, null, null, null, null, null, ContentSourceType.WEB);
        }
    }

    public record LlmContextResult(String url, String title, List<String> snippets, String hostname, String age) {
    }
}
