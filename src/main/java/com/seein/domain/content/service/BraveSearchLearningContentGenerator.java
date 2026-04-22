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
    public LearningContent generate(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, LocalDate publishedDate) {

        // 1. 학습 설정에 맞는 Brave 검색 조건을 만든다.
        BraveSearchQueryFactory.SearchQuery searchQuery = braveSearchQueryFactory.create(
                studyLanguage,
                learningStyle,
                difficultyLevel,
                publishedDate
        );

        // 2. Brave Search와 LLM Context에서 학습 콘텐츠 후보를 모은다.
        List<LearningContentCandidate> candidates = candidateCollector.collect(searchQuery);

        // 3. 후보마다 언어/출처/snippet/스타일/최신성 기준으로 점수를 매긴다.
        List<LearningContentQualityScorer.ScoredCandidate> scoredCandidates = scoreCandidates(candidates, studyLanguage, learningStyle);

        // 4. 기준 점수를 통과한 후보 중 가장 높은 점수의 후보를 선택한다.
        LearningContentQualityScorer.ScoredCandidate selectedCandidate = selectBestAcceptedCandidate(scoredCandidates);

        log.info("학습 콘텐츠 후보 선택 - style={}, language={}, score={}, url={}",
                learningStyle, studyLanguage, selectedCandidate.score(), selectedCandidate.candidate().url());

        // 5. 선택된 후보를 DB에 저장 가능한 학습 콘텐츠 카드로 조립한다.
        return cardAssembler.assemble(
                selectedCandidate,
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                publishedDate
        );
    }

    private List<LearningContentQualityScorer.ScoredCandidate> scoreCandidates(List<LearningContentCandidate> candidates, StudyLanguage studyLanguage, LearningStyle learningStyle) {
        return qualityScorer.score(candidates, studyLanguage, learningStyle);
    }

    private LearningContentQualityScorer.ScoredCandidate selectBestAcceptedCandidate(List<LearningContentQualityScorer.ScoredCandidate> scoredCandidates) {
        return scoredCandidates.stream()
                .filter(LearningContentQualityScorer.ScoredCandidate::accepted)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEARNING_CONTENT_GENERATION_FAILED,
                        "학습 콘텐츠로 사용할 수 있는 고품질 검색 결과를 찾지 못했습니다."
                ));
    }
}
