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

    Page<LearningContent> findByStudyLanguageAndExplanationLanguage(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            Pageable pageable
    );

    Page<LearningContent> findByStudyLanguageAndExplanationLanguageAndLearningStyle(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            Pageable pageable
    );

    Optional<LearningContent> findByStudyLanguageAndExplanationLanguageAndLearningStyleAndDifficultyLevelAndPublishedDate(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    );
}
