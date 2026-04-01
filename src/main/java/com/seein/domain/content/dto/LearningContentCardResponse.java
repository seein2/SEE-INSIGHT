package com.seein.domain.content.dto;

import com.seein.domain.content.entity.LearningContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 홈 피드/이메일 미리보기용 콘텐츠 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class LearningContentCardResponse {

    private final Integer contentId;
    private final String title;
    private final String summary;
    private final String sourceText;
    private final String explanationText;
    private final String expressionOne;
    private final String expressionTwo;
    private final String quizText;
    private final String sourceLink;
    private final String studyLanguage;
    private final String studyLanguageLabel;
    private final String explanationLanguage;
    private final String explanationLanguageLabel;
    private final String learningStyle;
    private final String learningStyleLabel;
    private final String difficultyLevel;
    private final String difficultyLevelLabel;
    private final LocalDate publishedDate;

    public static LearningContentCardResponse from(LearningContent content) {
        return new LearningContentCardResponse(
                content.getContentId(),
                content.getTitle(),
                content.getSummary(),
                content.getSourceText(),
                content.getExplanationText(),
                content.getExpressionOne(),
                content.getExpressionTwo(),
                content.getQuizText(),
                content.getSourceLink(),
                content.getStudyLanguage().name(),
                content.getStudyLanguage().getLabel(),
                content.getExplanationLanguage().name(),
                content.getExplanationLanguage().getLabel(),
                content.getLearningStyle().name(),
                content.getLearningStyle().getLabel(),
                content.getDifficultyLevel().name(),
                content.getDifficultyLevel().getLabel(),
                content.getPublishedDate()
        );
    }
}
