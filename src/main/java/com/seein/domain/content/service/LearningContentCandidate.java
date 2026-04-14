package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.global.config.BraveSearchClient;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Brave 검색/LLM Context에서 수집한 학습 콘텐츠 후보
 */
public record LearningContentCandidate(
        ContentSourceType sourceType,
        String title,
        String url,
        String description,
        List<String> snippets,
        String pageAge,
        String language,
        String sourceName,
        String sourceHost,
        String contentType,
        Boolean breaking
) {

    public static LearningContentCandidate from(BraveSearchClient.SearchResult result) {
        List<String> snippets = new ArrayList<>();
        if (StringUtils.hasText(result.description())) {
            snippets.add(result.description());
        }
        snippets.addAll(result.extraSnippets());
        String sourceHost = StringUtils.hasText(result.sourceHost()) ? result.sourceHost() : hostFromUrl(result.url());
        return new LearningContentCandidate(
                result.sourceType(),
                result.title(),
                result.url(),
                result.description(),
                snippets,
                result.pageAge(),
                result.language(),
                result.sourceName(),
                sourceHost,
                result.contentType(),
                result.breaking()
        );
    }

    public LearningContentCandidate withLlmContext(BraveSearchClient.LlmContextResult context) {
        List<String> mergedSnippets = new ArrayList<>(snippets);
        mergedSnippets.addAll(context.snippets());
        return new LearningContentCandidate(
                sourceType,
                StringUtils.hasText(title) ? title : context.title(),
                url,
                description,
                mergedSnippets,
                StringUtils.hasText(pageAge) ? pageAge : context.age(),
                language,
                sourceName,
                StringUtils.hasText(sourceHost) ? sourceHost : context.hostname(),
                contentType,
                breaking
        );
    }

    private static String hostFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        try {
            String host = URI.create(url).getHost();
            return StringUtils.hasText(host) ? host.replaceFirst("^www\\.", "") : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
