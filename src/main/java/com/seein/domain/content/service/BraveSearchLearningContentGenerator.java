package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Brave Search 기반 학습 콘텐츠 생성기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BraveSearchLearningContentGenerator implements LearningContentGenerator {

    private final BraveSearchQueryFactory braveSearchQueryFactory;
    private final LearningContentCandidateCollector candidateCollector;
    private final LearningContentQualityScorer qualityScorer;
    private final LearningContentCardAssembler cardAssembler;

    /**
     * Brave Search 결과를 바탕으로 학습 콘텐츠 생성
     */
    @Override
    public LearningContent generate(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        BraveSearchQueryFactory.SearchQuery searchQuery = braveSearchQueryFactory.create(
                studyLanguage,
                learningStyle,
                difficultyLevel,
                publishedDate
        );

        List<LearningContentQualityScorer.ScoredCandidate> scoredCandidates = qualityScorer.score(
                candidateCollector.collect(searchQuery),
                studyLanguage,
                learningStyle
        );

        LearningContentQualityScorer.ScoredCandidate selectedCandidate = scoredCandidates.stream()
                .filter(LearningContentQualityScorer.ScoredCandidate::accepted)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEARNING_CONTENT_GENERATION_FAILED,
                        "학습 콘텐츠로 사용할 수 있는 고품질 검색 결과를 찾지 못했습니다."
                ));

        log.info("학습 콘텐츠 후보 선택 - style={}, language={}, score={}, url={}",
                learningStyle, studyLanguage, selectedCandidate.score(), selectedCandidate.candidate().url());

        return cardAssembler.assemble(
                selectedCandidate,
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                publishedDate
        );
    }
}
