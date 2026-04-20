package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.global.config.BraveSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Brave Search와 LLM Context에서 학습 콘텐츠 후보를 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningContentCandidateCollector {

    private final BraveSearchClient braveSearchClient;

    // Brave Search와 LLM Context에서 학습 콘텐츠 후보를 수집한다.
    public List<LearningContentCandidate> collect(BraveSearchQueryFactory.SearchQuery searchQuery) {
        Map<String, LearningContentCandidate> candidatesByUrl = new LinkedHashMap<>();
        collectSearchResults(searchQuery, searchQuery.primaryFreshness(), candidatesByUrl);

        if (candidatesByUrl.isEmpty() && StringUtils.hasText(searchQuery.fallbackFreshness())) {
            collectSearchResults(searchQuery, searchQuery.fallbackFreshness(), candidatesByUrl);
        }

        if (searchQuery.useLlmContext()) {
            mergeLlmContext(searchQuery, candidatesByUrl);
        }

        return List.copyOf(candidatesByUrl.values());
    }

    // Brave Search에서 검색 결과를 수집하여 URL을 키로 후보 맵에 저장한다.
    private void collectSearchResults(BraveSearchQueryFactory.SearchQuery searchQuery, String freshness, Map<String, LearningContentCandidate> candidatesByUrl) {
        BraveSearchClient.SearchRequest request = new BraveSearchClient.SearchRequest(
                searchQuery.query(),
                searchQuery.country(),
                searchQuery.searchLanguage(),
                searchQuery.uiLanguage(),
                freshness,
                searchQuery.count(),
                true,
                true
        );

        // Brave Search API 호출(뉴스/웹 구분)
        List<BraveSearchClient.SearchResult> results = searchQuery.sourceType() == ContentSourceType.NEWS
                ? braveSearchClient.searchNews(request)
                : braveSearchClient.searchWeb(request);

        // 검색 결과 저장 - URL이 있는 결과만 후보로 추가
        for (BraveSearchClient.SearchResult result : results) {
            if (StringUtils.hasText(result.url())) {
                candidatesByUrl.putIfAbsent(result.url(), LearningContentCandidate.from(result));
            }
        }
    }

    // LLM Context에서 추가 후보를 가져와 기존 후보 맵과 병합한다. URL이 동일한 후보가 있으면 LLM Context 정보를 추가하고, 없으면 새 후보로 추가한다.
    private void mergeLlmContext(BraveSearchQueryFactory.SearchQuery searchQuery, Map<String, LearningContentCandidate> candidatesByUrl) {
        BraveSearchClient.LlmContextRequest request = new BraveSearchClient.LlmContextRequest(
                searchQuery.query(),
                searchQuery.country(),
                searchQuery.searchLanguage(),
                searchQuery.primaryFreshness(),
                Math.min(searchQuery.count(), 5),
                Math.min(searchQuery.count(), 5),
                4096,
                "strict"
        );

        List<BraveSearchClient.LlmContextResult> contexts;
        try {
            contexts = braveSearchClient.fetchLlmContext(request);
        } catch (Exception e) {
            log.warn("Brave LLM Context 후보 보강 실패, 검색 결과 후보만 사용합니다. query={}, error={}",
                    searchQuery.query(), e.getMessage());
            return;
        }

        for (BraveSearchClient.LlmContextResult context : contexts) {
            if (!StringUtils.hasText(context.url())) {
                continue;
            }

            LearningContentCandidate existing = candidatesByUrl.get(context.url());
            if (existing != null) {
                candidatesByUrl.put(context.url(), existing.withLlmContext(context));
            } else {
                candidatesByUrl.put(context.url(), new LearningContentCandidate(
                        searchQuery.sourceType(),
                        context.title(),
                        context.url(),
                        null,
                        context.snippets(),
                        context.age(),
                        searchQuery.searchLanguage(),
                        context.hostname(),
                        context.hostname(),
                        null,
                        null
                ));
            }
        }
    }
}
