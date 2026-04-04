package com.seein.domain.content.repository;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 학습 콘텐츠 Repository
 */
public interface LearningContentRepository extends JpaRepository<LearningContent, Integer> {

    /*
    * 학습 콘텐츠 조회
        - 언어 설정(학습 언어, 설명 언어)과 학습 스타일을 기준으로 페이징 처리된 콘텐츠 목록을 조회하는 메서드
        - 학습 스타일이 null인 경우에는 모든 학습 스타일에 해당하는 콘텐츠를 조회
     */
    Page<LearningContent> findByStudyLanguageAndExplanationLanguage(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            Pageable pageable
    );

    /*
    * 학습 콘텐츠 조회
        - 언어 설정(학습 언어, 설명 언어)과 학습 스타일을 기준으로 페이징 처리된 콘텐츠 목록을 조회하는 메서드
     */
    Page<LearningContent> findByStudyLanguageAndExplanationLanguageAndLearningStyle(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            Pageable pageable
    );

    /*
    * 학습 콘텐츠 조회
        - 언어 설정(학습 언어, 설명 언어), 학습 스타일, 난이도, 그리고 게시 날짜를 기준으로 콘텐츠를 조회하는 메서드
     */
    Optional<LearningContent> findByStudyLanguageAndExplanationLanguageAndLearningStyleAndDifficultyLevelAndPublishedDate(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    );
}
