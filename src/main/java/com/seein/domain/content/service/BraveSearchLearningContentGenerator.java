package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.config.BraveSearchClient;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * Brave Search 기반 학습 콘텐츠 생성기
 */
@Component
@RequiredArgsConstructor
public class BraveSearchLearningContentGenerator implements LearningContentGenerator {

    private final BraveSearchClient braveSearchClient;
    private final BraveSearchQueryFactory braveSearchQueryFactory;
    private final LearningContentTemplateFactory templateFactory;

    /**
     * Brave Search 결과를 바탕으로 학습 콘텐츠 생성
     */
    @Override
    public LearningContent generate(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, LocalDate publishedDate) {
        BraveSearchQueryFactory.SearchQuery searchQuery = braveSearchQueryFactory.create(
                studyLanguage,
                learningStyle,
                difficultyLevel,
                publishedDate
        );

        BraveSearchClient.SearchResult result = findUsableResult(
                searchQuery.query(),
                studyLanguage.getSearchLanguageCode(),
                searchQuery.primaryFreshness(),
                searchQuery.fallbackFreshness()
        );

        String title = sanitizeTitle(result.title());
        String sourceText = sanitizeSourceText(result.description());
        LearningContentTemplateFactory.ExpressionPair expressions = templateFactory.extractExpressions(studyLanguage, sourceText);

        return LearningContent.create(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                title,
                templateFactory.createSummary(explanationLanguage, learningStyle, difficultyLevel, title),
                sourceText,
                templateFactory.createExplanation(explanationLanguage, learningStyle, difficultyLevel, title),
                expressions.expressionOne(),
                expressions.expressionTwo(),
                templateFactory.createQuiz(explanationLanguage, learningStyle, title),
                result.url(),
                publishedDate
        );
    }

    /*
     * Brave Search에서 유효한 결과를 찾는 로직
     */
    private BraveSearchClient.SearchResult findUsableResult(String query, String searchLanguageCode, String primaryFreshness, String fallbackFreshness) {
        BraveSearchClient.SearchResult result = firstUsable(braveSearchClient.searchWeb(query, searchLanguageCode, primaryFreshness));
        if (result != null) {
            return result;
        }

        // 첫 번째 검색에서 유효한 결과가 없으면, 두 번째 검색으로 신선도 기준을 완화하여 다시 시도
        result = firstUsable(braveSearchClient.searchWeb(query, searchLanguageCode, fallbackFreshness));
        if (result != null) {
            return result;
        }

        throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED, "Brave Search에서 유효한 콘텐츠를 찾지 못했습니다.");
    }

    /*
     * Brave Search 결과 목록에서 제목, 설명, URL이 모두 존재하는 첫 번째 결과를 반환
     */
    private BraveSearchClient.SearchResult firstUsable(List<BraveSearchClient.SearchResult> results) {
        return results.stream()
                .filter(result -> StringUtils.hasText(result.title())
                        && StringUtils.hasText(result.description())
                        && StringUtils.hasText(result.url()))
                .findFirst()
                .orElse(null);
    }

    /*
     * Brave Search 결과에서 제목을 간소화하여 추출(불필요한 접미사 제거, 양끝 공백 제거)
     */
    private String sanitizeTitle(String rawTitle) {
        String trimmed = rawTitle.trim();
        String[] separators = {" | ", " - "};
        for (String separator : separators) {
            int index = trimmed.indexOf(separator);
            if (index > 0) {
                return trimmed.substring(0, index).trim();
            }
        }
        return trimmed;
    }

    /*
     * Brave Search 결과에서 본문을 간소화하여 추출 (여러 공백을 하나로 축소하고 양끝 공백 제거)
     */
    private String sanitizeSourceText(String rawDescription) {
        return rawDescription.replaceAll("\\s+", " ").trim();
    }
}
