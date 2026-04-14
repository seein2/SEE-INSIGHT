package com.seein.domain.content.dto;

import com.seein.domain.content.entity.LearningContent;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 홈 피드/이메일 미리보기용 콘텐츠 응답 DTO
 */
@Getter
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
    private final String contentSourceType;
    private final String sourceName;
    private final String sourceHost;
    private final Integer qualityScore;
    private final LocalDate publishedDate;

    public LearningContentCardResponse(
            Integer contentId,
            String title,
            String summary,
            String sourceText,
            String explanationText,
            String expressionOne,
            String expressionTwo,
            String quizText,
            String sourceLink,
            String studyLanguage,
            String studyLanguageLabel,
            String explanationLanguage,
            String explanationLanguageLabel,
            String learningStyle,
            String learningStyleLabel,
            String difficultyLevel,
            String difficultyLevelLabel,
            LocalDate publishedDate
    ) {
        this(
                contentId,
                title,
                summary,
                sourceText,
                explanationText,
                expressionOne,
                expressionTwo,
                quizText,
                sourceLink,
                studyLanguage,
                studyLanguageLabel,
                explanationLanguage,
                explanationLanguageLabel,
                learningStyle,
                learningStyleLabel,
                difficultyLevel,
                difficultyLevelLabel,
                null,
                null,
                null,
                null,
                publishedDate
        );
    }

    public LearningContentCardResponse(
            Integer contentId,
            String title,
            String summary,
            String sourceText,
            String explanationText,
            String expressionOne,
            String expressionTwo,
            String quizText,
            String sourceLink,
            String studyLanguage,
            String studyLanguageLabel,
            String explanationLanguage,
            String explanationLanguageLabel,
            String learningStyle,
            String learningStyleLabel,
            String difficultyLevel,
            String difficultyLevelLabel,
            String contentSourceType,
            String sourceName,
            String sourceHost,
            Integer qualityScore,
            LocalDate publishedDate
    ) {
        this.contentId = contentId;
        this.title = title;
        this.summary = summary;
        this.sourceText = sourceText;
        this.explanationText = explanationText;
        this.expressionOne = expressionOne;
        this.expressionTwo = expressionTwo;
        this.quizText = quizText;
        this.sourceLink = sourceLink;
        this.studyLanguage = studyLanguage;
        this.studyLanguageLabel = studyLanguageLabel;
        this.explanationLanguage = explanationLanguage;
        this.explanationLanguageLabel = explanationLanguageLabel;
        this.learningStyle = learningStyle;
        this.learningStyleLabel = learningStyleLabel;
        this.difficultyLevel = difficultyLevel;
        this.difficultyLevelLabel = difficultyLevelLabel;
        this.contentSourceType = contentSourceType;
        this.sourceName = sourceName;
        this.sourceHost = sourceHost;
        this.qualityScore = qualityScore;
        this.publishedDate = publishedDate;
    }

    /**
     * 학습 콘텐츠 카드 응답 변환
     */
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
                content.getContentSourceType() != null ? content.getContentSourceType().name() : null,
                content.getSourceName(),
                content.getSourceHost(),
                content.getQualityScore(),
                content.getPublishedDate()
        );
    }
}
