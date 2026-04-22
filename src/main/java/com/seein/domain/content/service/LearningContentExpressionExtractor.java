package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 정제된 원문에서 신뢰 가능한 짧은 표현만 추출
 */
@Component
public class LearningContentExpressionExtractor {

    /**
     * 표현 2개 추출
     */
    public LearningContentTemplateFactory.ExpressionPair extractExpressions(StudyLanguage studyLanguage, String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return new LearningContentTemplateFactory.ExpressionPair(null, null);
        }

        List<String> expressions = switch (studyLanguage) {
            case ENGLISH -> extractEnglishExpressions(sourceText);
            case JAPANESE, CHINESE -> extractCjkExpressions(sourceText);
        };

        String expressionOne = expressions.size() > 0 ? expressions.get(0) : null;
        String expressionTwo = expressions.size() > 1 ? expressions.get(1) : null;
        return new LearningContentTemplateFactory.ExpressionPair(expressionOne, expressionTwo);
    }

    private List<String> extractEnglishExpressions(String sourceText) {
        List<String> expressions = new ArrayList<>();
        for (String segment : sourceText.split("[.!?]")) {
            String candidate = segment.replaceAll("\\s+", " ").trim();
            if (isCleanEnglishPhrase(candidate) && !expressions.contains(candidate)) {
                expressions.add(candidate);
            }
            if (expressions.size() >= 2) {
                break;
            }
        }
        return expressions;
    }

    private List<String> extractCjkExpressions(String sourceText) {
        List<String> expressions = new ArrayList<>();
        for (String segment : sourceText.split("[。！？.!?]")) {
            String candidate = segment.replaceAll("\\s+", "").trim();
            if (candidate.length() >= 6 && candidate.length() <= 24 && !containsMarkupArtifact(candidate)) {
                expressions.add(candidate);
            }
            if (expressions.size() >= 2) {
                break;
            }
        }
        return expressions;
    }

    private boolean isCleanEnglishPhrase(String candidate) {
        if (!StringUtils.hasText(candidate) || containsMarkupArtifact(candidate)) {
            return false;
        }

        String[] words = candidate.split(" ");
        return words.length >= 3
                && words.length <= 9
                && candidate.length() <= 70
                && candidate.matches(".*[A-Za-z].*")
                && !candidate.matches("^[A-Z][A-Za-z0-9 .'-]{0,25}$");
    }

    private boolean containsMarkupArtifact(String candidate) {
        return candidate.contains("<")
                || candidate.contains(">")
                || candidate.contains("&#")
                || candidate.contains("&lt;")
                || candidate.contains("&gt;")
                || candidate.toLowerCase().contains("strong");
    }
}
