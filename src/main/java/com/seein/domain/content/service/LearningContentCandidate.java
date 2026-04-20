package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.global.config.BraveSearchClient;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Brave 검색/LLM Context에서 수집한 학습 콘텐츠 후보
 *
 * record인 이유:
 * - 후보 데이터는 생성 후 수정하지 않는 값 객체에 가깝다.
 * - title, url, snippets, sourceHost 같은 필드를 묶어 다음 단계(품질 점수화, 카드 조립)로 전달한다.
 *
 * 이 객체는 DB 엔티티가 아니라 "저장 전 임시 후보"다.
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

    /*
     * Brave web/search 또는 news/search 결과를 내부 후보 객체로 변환한다.
     *
     * BraveSearchClient.SearchResult는 외부 API 응답에 가까운 형태이고,
     * LearningContentCandidate는 우리 서비스의 품질 점수화에 필요한 형태다.
     */
    public static LearningContentCandidate from(BraveSearchClient.SearchResult result) {
        List<String> snippets = new ArrayList<>();

        // description도 학습 원문 후보이므로 snippets 목록에 먼저 넣는다.
        if (StringUtils.hasText(result.description())) {
            snippets.add(result.description());
        }

        // extra_snippets가 있으면 함께 넣어서 품질 점수화에 사용할 텍스트 양을 늘린다.
        snippets.addAll(result.extraSnippets());

        // Brave 응답에 sourceHost가 없을 수 있으므로, 없으면 URL에서 host를 추출한다.
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

    /*
     * 기존 후보에 Brave LLM Context 결과를 병합한다.
     *
     * web/search 결과는 title/description/URL 중심이고,
     * LLM Context 결과는 본문에 가까운 snippets를 더 제공한다.
     *
     * 따라서 같은 URL의 후보가 이미 있다면 새 객체를 만들어 snippets를 보강한다.
     * record는 불변 객체처럼 다루는 것이 안전하므로 기존 객체를 직접 수정하지 않는다.
     */
    public LearningContentCandidate withLlmContext(BraveSearchClient.LlmContextResult context) {
        // 기존 snippet과 LLM Context snippet을 합쳐 더 풍부한 원문 후보를 만든다.
        List<String> mergedSnippets = new ArrayList<>(snippets);
        mergedSnippets.addAll(context.snippets());

        return new LearningContentCandidate(
                sourceType,
                // 기존 title이 있으면 유지하고, 없을 때만 LLM Context title을 사용한다.
                StringUtils.hasText(title) ? title : context.title(),
                url,
                description,
                mergedSnippets,
                // 기존 pageAge가 있으면 유지하고, 없을 때만 LLM Context age를 사용한다.
                StringUtils.hasText(pageAge) ? pageAge : context.age(),
                language,
                sourceName,
                // 기존 sourceHost가 있으면 유지하고, 없을 때만 LLM Context hostname을 사용한다.
                StringUtils.hasText(sourceHost) ? sourceHost : context.hostname(),
                contentType,
                breaking
        );
    }

    /*
     * URL에서 host만 추출한다.
     *
     * 예:
     * - https://www.bbc.com/news/world -> bbc.com
     * - https://example.com/article -> example.com
     *
     * URL이 비어 있거나 잘못된 형식이면 null을 반환한다.
     */
    private static String hostFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        try {
            String host = URI.create(url).getHost();
            // www.는 같은 도메인 비교를 방해하므로 제거한다.
            return StringUtils.hasText(host) ? host.replaceFirst("^www\\.", "") : null;
        } catch (IllegalArgumentException e) {
            // 외부 API 응답 URL이 깨져 있어도 후보 생성 전체가 실패하지 않게 한다.
            return null;
        }
    }
}
