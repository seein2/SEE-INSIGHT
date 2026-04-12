package com.seein.domain.content.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.content.repository.LearningContentRepository;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final LearningContentGenerator learningContentGenerator;
    private final LearningContentFallbackFactory fallbackFactory;

    /**
     * 학습 피드 카드 목록 조회
     */
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
        return cards;
    }

    /**
     * 일일 콘텐츠 조회 또는 생성
     */
    @Transactional
    public LearningContent getOrCreateDailyContent(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, LocalDate publishedDate) {
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

    /**
     * 미리보기 콘텐츠 조회
     */
    public LearningContentCardResponse getPreviewContent(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        LearningContent content = getOrCreateDailyContent(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                LocalDate.now()
        );
        return LearningContentCardResponse.from(content);
    }

    /**
     * 학습 콘텐츠 생성
     */
    private LearningContent generateContent(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, LocalDate publishedDate) {
        try {
            return learningContentGenerator.generate(
                    studyLanguage,
                    explanationLanguage,
                    learningStyle,
                    difficultyLevel,
                    publishedDate
            );
        } catch (Exception e) {
            log.warn("학습 콘텐츠 생성 실패, 기본 콘텐츠로 대체합니다. studyLanguage={}, style={}, difficulty={}, error={}", studyLanguage, learningStyle, difficultyLevel, e.getMessage());
            return fallbackFactory.createDailyContent(
                    studyLanguage,
                    explanationLanguage,
                    learningStyle,
                    difficultyLevel,
                    publishedDate
            );
        }
    }
}
