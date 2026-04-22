package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.global.config.BraveSearchClient;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningContentCandidateCollectorTest {

    @Mock
    private BraveSearchClient braveSearchClient;

    @Test
    @DisplayName("LLM Context 보강이 실패해도 web/search 후보는 유지한다")
    void collect_keepsWebCandidatesWhenLlmContextFails() {
        // given
        LearningContentCandidateCollector collector = new LearningContentCandidateCollector(braveSearchClient);
        BraveSearchQueryFactory.SearchQuery searchQuery = new BraveSearchQueryFactory.SearchQuery(
                "English article practical phrase short learning",
                "US",
                "en",
                "en-US",
                ContentSourceType.WEB,
                "pw",
                "pm",
                true,
                8
        );
        given(braveSearchClient.searchWeb(org.mockito.ArgumentMatchers.any(BraveSearchClient.SearchRequest.class)))
                .willReturn(List.of(new BraveSearchClient.SearchResult(
                        "Daily habits",
                        "A small daily habit often matters more than a perfect long plan.",
                        "https://example.com/article"
                )));
        given(braveSearchClient.fetchLlmContext(org.mockito.ArgumentMatchers.any(BraveSearchClient.LlmContextRequest.class)))
                .willThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        // when
        List<LearningContentCandidate> candidates = collector.collect(searchQuery);

        // then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).url()).isEqualTo("https://example.com/article");
    }

    @Test
    @DisplayName("뉴스 스타일 후보는 news/search 요청으로 수집한다")
    void collect_usesNewsSearchForNewsSourceType() {
        // given
        LearningContentCandidateCollector collector = new LearningContentCandidateCollector(braveSearchClient);
        BraveSearchQueryFactory.SearchQuery searchQuery = new BraveSearchQueryFactory.SearchQuery(
                "short local news article English learners beginner",
                "US",
                "en",
                "en-US",
                ContentSourceType.NEWS,
                "pd",
                "pw",
                false,
                8
        );
        given(braveSearchClient.searchNews(org.mockito.ArgumentMatchers.any(BraveSearchClient.SearchRequest.class)))
                .willReturn(List.of(new BraveSearchClient.SearchResult(
                        "Local news",
                        "A local council announced a new transport plan for residents.",
                        "https://news.example.com/local"
                )));

        // when
        List<LearningContentCandidate> candidates = collector.collect(searchQuery);

        // then
        ArgumentCaptor<BraveSearchClient.SearchRequest> requestCaptor =
                ArgumentCaptor.forClass(BraveSearchClient.SearchRequest.class);
        verify(braveSearchClient).searchNews(requestCaptor.capture());
        assertThat(requestCaptor.getValue().freshness()).isEqualTo("pd");
        assertThat(requestCaptor.getValue().extraSnippets()).isTrue();
        assertThat(requestCaptor.getValue().includeFetchMetadata()).isTrue();
        assertThat(candidates).hasSize(1);
    }
}
