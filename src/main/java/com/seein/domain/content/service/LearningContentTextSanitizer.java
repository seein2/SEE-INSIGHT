package com.seein.domain.content.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 외부 검색 결과 텍스트 정제
 */
@Component
public class LearningContentTextSanitizer {

    private static final int MAX_SOURCE_LENGTH = 360;

    /**
     * 제목 정제
     */
    public String sanitizeTitle(String rawTitle) {
        String cleaned = sanitizeText(rawTitle);
        if (!StringUtils.hasText(cleaned)) {
            return "오늘의 학습 콘텐츠";
        }

        String[] separators = {" | ", " - ", " – ", " — "};
        for (String separator : separators) {
            int index = cleaned.indexOf(separator);
            if (index > 0) {
                return cleaned.substring(0, index).trim();
            }
        }
        return limitLength(cleaned, 100);
    }

    /**
     * 원문 후보 snippet들을 하나의 짧은 학습 원문으로 정제
     */
    public String sanitizeSourceText(List<String> rawSnippets) {
        if (rawSnippets == null || rawSnippets.isEmpty()) {
            return null;
        }

        return rawSnippets.stream()
                .map(this::sanitizeSnippet)
                .filter(this::isUsableSourceText)
                .distinct()
                .findFirst()
                .orElse(null);
    }

    /**
     * 단일 snippet 정제
     */
    public String sanitizeSnippet(String rawSnippet) {
        String cleaned = sanitizeText(rawSnippet);
        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        cleaned = cleaned
                .replaceAll("\\s*(\\.\\.\\.|…)+\\s*$", "")
                .replaceAll("\\s+", " ")
                .trim();

        return limitAtSentenceBoundary(cleaned, MAX_SOURCE_LENGTH);
    }

    public boolean isUsableSourceText(String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return false;
        }

        return sourceText.length() >= 35
                && !sourceText.matches(".*<[^>]+>.*")
                && !sourceText.matches(".*&[#A-Za-z0-9]+;.*")
                && !sourceText.contains("&#")
                && !sourceText.contains("</")
                && !sourceText.endsWith("...");
    }

    private String sanitizeText(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return null;
        }

        String cleaned = HtmlUtils.htmlUnescape(HtmlUtils.htmlUnescape(rawText))
                .replaceAll("(?i)</?(strong|b|em|i|mark)>", "")
                .replaceAll("<[^>]+>", " ")
                .replace('\u00a0', ' ')
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('“', '"')
                .replace('”', '"')
                .replaceAll("\\s+", " ")
                .trim();
        return StringUtils.hasText(cleaned) ? cleaned : null;
    }

    private String limitAtSentenceBoundary(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        String limited = text.substring(0, maxLength).trim();
        int boundary = Math.max(
                Math.max(limited.lastIndexOf('.'), limited.lastIndexOf('!')),
                Math.max(limited.lastIndexOf('?'), Math.max(limited.lastIndexOf('。'), limited.lastIndexOf('！')))
        );

        if (boundary >= 80) {
            return limited.substring(0, boundary + 1).trim();
        }
        return limited.replaceAll("[,;:、，]\\s*[^,;:、，]*$", "").trim();
    }

    private String limitLength(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength).trim();
    }
}
