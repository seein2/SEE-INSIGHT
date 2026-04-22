package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LearningContentTemplateFactoryTest {

    private final LearningContentTemplateFactory templateFactory = new LearningContentTemplateFactory();

    @Test
    @DisplayName("요약은 제목 반복 없이 학습 목표를 짧게 전달한다")
    void createSummary_returnsLearningGoalWithoutTitle() {
        // when
        String summary = templateFactory.createSummary(
                ExplanationLanguage.KOREAN,
                LearningStyle.NEWS_READING,
                DifficultyLevel.BEGINNER
        );

        // then
        assertThat(summary).contains("짧은 원문");
        assertThat(summary).doesNotContain("카드", "'");
    }

    @Test
    @DisplayName("학습 포인트는 본문을 읽을 때 바로 따라 할 행동을 안내한다")
    void createExplanation_returnsConcreteReadingAction() {
        // when
        String explanation = templateFactory.createExplanation(
                ExplanationLanguage.KOREAN,
                LearningStyle.NEWS_READING,
                DifficultyLevel.BEGINNER
        );

        // then
        assertThat(explanation).contains("사람·기관", "행동", "시간");
        assertThat(explanation).contains("표시하세요");
    }

    @Test
    @DisplayName("문제는 제목 없이 복습 행동을 유도한다")
    void createQuiz_returnsReviewActionWithoutTitle() {
        // when
        String quiz = templateFactory.createQuiz(
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED
        );

        // then
        assertThat(quiz).contains("새 문장");
        assertThat(quiz).doesNotContain("'");
    }

    @Test
    @DisplayName("영어 설명에는 한국어 난이도 라벨이 섞이지 않는다")
    void createSummary_englishDoesNotContainKoreanDifficultyLabel() {
        // when
        String summary = templateFactory.createSummary(
                ExplanationLanguage.ENGLISH,
                LearningStyle.NEWS_READING,
                DifficultyLevel.BEGINNER
        );

        // then
        assertThat(summary).contains("short excerpt");
        assertThat(summary).doesNotContain("초급");
    }
}
