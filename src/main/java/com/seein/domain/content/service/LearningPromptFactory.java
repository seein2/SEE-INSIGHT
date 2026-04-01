package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.stereotype.Component;

/**
 * 학습 콘텐츠 생성용 프롬프트 팩토리
 */
@Component
public class LearningPromptFactory {

    public PromptBundle create(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel
    ) {
        String systemPrompt = """
                You are a language-learning editor.
                Search for a recent authentic topic in the target language and return only valid JSON.
                The JSON keys must be:
                title, summary, sourceText, explanationText, expressionOne, expressionTwo, quizText.
                Keep sourceText in the target study language.
                Keep summary, explanationText, and quizText in the explanation language.
                Make the content concise, practical, and email-ready.
                Do not wrap the JSON in markdown fences.
                """;

        String userPrompt = String.format(
                """
                        Create one learning content item.
                        Study language: %s
                        Explanation language: %s
                        Style: %s
                        Difficulty: %s
                        Requirements:
                        - title: short and engaging
                        - summary: 1-2 sentences
                        - sourceText: one short authentic passage or sentence set
                        - explanationText: clear learning explanation
                        - expressionOne/expressionTwo: useful phrases from the content
                        - quizText: one short review question
                        """,
                studyLanguage.getLabel(),
                explanationLanguage.getLabel(),
                learningStyle.getLabel(),
                difficultyLevel.getLabel()
        );

        return new PromptBundle(systemPrompt, userPrompt, studyLanguage.getSearchLanguageCode());
    }

    public record PromptBundle(String systemPrompt, String userPrompt, String searchLanguageCode) {
    }
}
