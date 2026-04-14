package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * 정제된 후보를 learning_content 엔티티로 조립
 */
@Component
@RequiredArgsConstructor
public class LearningContentCardAssembler {

    public static final String GENERATION_VERSION = "brave-v2";

    private final LearningContentTextSanitizer textSanitizer;
    private final LearningContentExpressionExtractor expressionExtractor;
    private final LearningContentTemplateFactory templateFactory;

    /**
     * 점수화된 후보를 학습 콘텐츠로 변환
     */
    public LearningContent assemble(
            LearningContentQualityScorer.ScoredCandidate scoredCandidate,
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        LearningContentCandidate candidate = scoredCandidate.candidate();
        String title = textSanitizer.sanitizeTitle(candidate.title());
        String sourceText = textSanitizer.sanitizeSourceText(candidate.snippets());
        LearningContentTemplateFactory.ExpressionPair expressions = expressionExtractor.extractExpressions(studyLanguage, sourceText);
        String rawSnippets = candidate.snippets().stream()
                .limit(5)
                .collect(Collectors.joining("\n---\n"));

        return LearningContent.createWithMetadata(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                title,
                templateFactory.createSummary(explanationLanguage, learningStyle, difficultyLevel, title),
                sourceText,
                templateFactory.createExplanation(explanationLanguage, learningStyle, difficultyLevel, title, sourceText),
                expressions.expressionOne(),
                expressions.expressionTwo(),
                templateFactory.createQuiz(explanationLanguage, learningStyle, title),
                candidate.url(),
                candidate.sourceType(),
                candidate.sourceName(),
                candidate.sourceHost(),
                candidate.pageAge(),
                scoredCandidate.score(),
                rawSnippets,
                GENERATION_VERSION,
                publishedDate
        );
    }
}
