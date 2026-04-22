package com.seein.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.domain.content.entity.ContentSourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BraveSearchClientMappingTest {

    private final BraveSearchClient client = new BraveSearchClient(createProperties(), new ObjectMapper());

    @Test
    @DisplayName("Brave web/search 응답의 주요 메타데이터를 매핑한다")
    void extractSearchResults_mapsWebMetadata() {
        // given
        String responseJson = """
                {
                  "web": {
                    "results": [
                      {
                        "title": "Daily habits | Example News",
                        "description": "A small daily habit often matters more than a perfect long plan.",
                        "url": "https://example.com/article",
                        "extra_snippets": ["Extra context for learners."],
                        "page_age": "2 days ago",
                        "language": "en",
                        "meta_url": {"hostname": "example.com"},
                        "profile": {"name": "Example News"},
                        "thumbnail": {"src": "https://example.com/thumb.jpg"},
                        "content_type": "article",
                        "breaking": false,
                        "fetched_at": "2026-04-12T00:00:00Z"
                      }
                    ]
                  }
                }
                """;

        // when
        List<BraveSearchClient.SearchResult> results = client.extractSearchResults(responseJson, ContentSourceType.WEB);

        // then
        assertThat(results).hasSize(1);
        BraveSearchClient.SearchResult result = results.get(0);
        assertThat(result.title()).isEqualTo("Daily habits | Example News");
        assertThat(result.extraSnippets()).containsExactly("Extra context for learners.");
        assertThat(result.pageAge()).isEqualTo("2 days ago");
        assertThat(result.language()).isEqualTo("en");
        assertThat(result.sourceName()).isEqualTo("Example News");
        assertThat(result.sourceHost()).isEqualTo("example.com");
        assertThat(result.contentType()).isEqualTo("article");
        assertThat(result.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(result.sourceType()).isEqualTo(ContentSourceType.WEB);
    }

    @Test
    @DisplayName("Brave news/search 응답은 results 배열에서 뉴스 후보를 매핑한다")
    void extractSearchResults_mapsNewsResultsArray() {
        // given
        String responseJson = """
                {
                  "results": [
                    {
                      "title": "Local news",
                      "description": "A local council announced a new transport plan for residents.",
                      "url": "https://news.example.com/local",
                      "extra_snippets": ["Residents can use buses more often."],
                      "age": "1 day ago",
                      "meta_url": {"hostname": "news.example.com"},
                      "profile": {"name": "News Example"},
                      "breaking": true
                    }
                  ]
                }
                """;

        // when
        List<BraveSearchClient.SearchResult> results = client.extractSearchResults(responseJson, ContentSourceType.NEWS);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).sourceType()).isEqualTo(ContentSourceType.NEWS);
        assertThat(results.get(0).pageAge()).isEqualTo("1 day ago");
        assertThat(results.get(0).breaking()).isTrue();
    }

    @Test
    @DisplayName("Brave LLM Context 응답의 grounding과 sources 메타데이터를 매핑한다")
    void extractLlmContextResults_mapsGroundingAndSources() {
        // given
        String responseJson = """
                {
                  "grounding": {
                    "generic": [
                      {
                        "url": "https://example.com/page",
                        "title": "Page Title",
                        "snippets": ["Relevant text chunk extracted from the page."]
                      }
                    ],
                    "map": []
                  },
                  "sources": {
                    "https://example.com/page": {
                      "title": "Page Title",
                      "hostname": "example.com",
                      "age": ["Sunday, April 12, 2026", "2026-04-12", "0 days ago"]
                    }
                  }
                }
                """;

        // when
        List<BraveSearchClient.LlmContextResult> results = client.extractLlmContextResults(responseJson);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).url()).isEqualTo("https://example.com/page");
        assertThat(results.get(0).title()).isEqualTo("Page Title");
        assertThat(results.get(0).snippets()).containsExactly("Relevant text chunk extracted from the page.");
        assertThat(results.get(0).hostname()).isEqualTo("example.com");
        assertThat(results.get(0).age()).contains("0 days ago");
    }

    private BraveProperties createProperties() {
        BraveProperties properties = new BraveProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://api.search.brave.com");
        return properties;
    }
}
