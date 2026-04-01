package com.seein.domain.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.repository.LearningContentRepository;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.config.PerplexityClient;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * 학습 콘텐츠 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningContentService {

    private static final int FEED_LIMIT = 6;

    private final LearningContentRepository learningContentRepository;
    private final PerplexityClient perplexityClient;
    private final LearningPromptFactory learningPromptFactory;
    private final LearningContentFallbackFactory fallbackFactory;
    private final ObjectMapper objectMapper;

    public List<LearningContentCardResponse> getFeedCards(StudyLanguage studyLanguage, LearningStyle learningStyle) {
        ExplanationLanguage explanationLanguage = ExplanationLanguage.KOREAN;
        PageRequest pageRequest = PageRequest.of(0, FEED_LIMIT, Sort.by(
                Sort.Order.desc("publishedDate"),
                Sort.Order.desc("createdAt")
        ));

        List<LearningContentCardResponse> cards = (learningStyle == null
                ? learningContentRepository.findByStudyLanguageAndExplanationLanguage(studyLanguage, explanationLanguage, pageRequest)
                : learningContentRepository.findByStudyLanguageAndExplanationLanguageAndLearningStyle(studyLanguage, explanationLanguage, learningStyle, pageRequest)
        ).stream().map(LearningContentCardResponse::from).toList();

        if (cards.size() >= FEED_LIMIT) {
            return cards;
        }

        List<LearningContentCardResponse> fallbackCards = fallbackFactory.createFeedContents(studyLanguage, explanationLanguage)
                .stream()
                .filter(content -> learningStyle == null || content.getLearningStyle() == learningStyle)
                .map(LearningContentCardResponse::from)
                .toList();

        LinkedHashSet<String> deduplicatedTitles = new LinkedHashSet<>();
        List<LearningContentCardResponse> merged = new ArrayList<>();

        for (LearningContentCardResponse card : cards) {
            if (deduplicatedTitles.add(card.getTitle())) {
                merged.add(card);
            }
        }

        for (LearningContentCardResponse card : fallbackCards) {
            if (merged.size() >= FEED_LIMIT) {
                break;
            }
            if (deduplicatedTitles.add(card.getTitle())) {
                merged.add(card);
            }
        }

        return merged;
    }

    @Transactional
    public LearningContent getOrCreateDailyContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        Optional<LearningContent> existingContent = learningContentRepository
                .findByStudyLanguageAndExplanationLanguageAndLearningStyleAndDifficultyLevelAndPublishedDate(
                        studyLanguage,
                        explanationLanguage,
                        learningStyle,
                        difficultyLevel,
                        publishedDate
                );

        if (existingContent.isPresent()) {
            return existingContent.get();
        }

        LearningContent generated = generateContent(studyLanguage, explanationLanguage, learningStyle, difficultyLevel, publishedDate);
        return learningContentRepository.save(generated);
    }

    public LearningContentCardResponse getPreviewContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel
    ) {
        LearningContent content = getOrCreateDailyContent(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                LocalDate.now()
        );
        return LearningContentCardResponse.from(content);
    }

    private LearningContent generateContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        try {
            LearningPromptFactory.PromptBundle prompt = learningPromptFactory.create(
                    studyLanguage,
                    explanationLanguage,
                    learningStyle,
                    difficultyLevel
            );
            String responseJson = perplexityClient.generateLearningContent(
                    prompt.systemPrompt(),
                    prompt.userPrompt(),
                    prompt.searchLanguageCode()
            );
            String contentJson = stripMarkdownFence(perplexityClient.extractContentFromResponse(responseJson));
            JsonNode contentNode = objectMapper.readTree(contentJson);

            return LearningContent.create(
                    studyLanguage,
                    explanationLanguage,
                    learningStyle,
                    difficultyLevel,
                    requiredText(contentNode, "title"),
                    requiredText(contentNode, "summary"),
                    requiredText(contentNode, "sourceText"),
                    requiredText(contentNode, "explanationText"),
                    optionalText(contentNode, "expressionOne"),
                    optionalText(contentNode, "expressionTwo"),
                    optionalText(contentNode, "quizText"),
                    perplexityClient.extractFirstCitation(responseJson),
                    publishedDate
            );
        } catch (Exception e) {
            log.warn("학습 콘텐츠 생성 실패, 기본 콘텐츠로 대체합니다. studyLanguage={}, style={}, difficulty={}, error={}",
                    studyLanguage, learningStyle, difficultyLevel, e.getMessage());
            return fallbackFactory.createDailyContent(
                    studyLanguage,
                    explanationLanguage,
                    learningStyle,
                    difficultyLevel,
                    publishedDate
            );
        }
    }

    private String stripMarkdownFence(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        String trimmed = rawContent.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        return trimmed
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }

    private String requiredText(JsonNode contentNode, String fieldName) {
        String value = optionalText(contentNode, fieldName);
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED, fieldName + " 값이 비어 있습니다.");
        }
        return value;
    }

    private String optionalText(JsonNode contentNode, String fieldName) {
        JsonNode node = contentNode.path(fieldName);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }
}
