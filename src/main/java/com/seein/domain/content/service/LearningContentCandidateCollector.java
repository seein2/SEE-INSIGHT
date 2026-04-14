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

    /**
     * 검색 profile 기준 후보 수집
     */
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

    private void collectSearchResults(
            BraveSearchQueryFactory.SearchQuery searchQuery,
            String freshness,
            Map<String, LearningContentCandidate> candidatesByUrl
    ) {
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

        List<BraveSearchClient.SearchResult> results = searchQuery.sourceType() == ContentSourceType.NEWS
                ? braveSearchClient.searchNews(request)
                : braveSearchClient.searchWeb(request);

        for (BraveSearchClient.SearchResult result : results) {
            if (StringUtils.hasText(result.url())) {
                candidatesByUrl.putIfAbsent(result.url(), LearningContentCandidate.from(result));
            }
        }
    }

    private void mergeLlmContext(
            BraveSearchQueryFactory.SearchQuery searchQuery,
            Map<String, LearningContentCandidate> candidatesByUrl
    ) {
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
