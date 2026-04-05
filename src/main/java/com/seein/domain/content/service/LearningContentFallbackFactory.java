package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Brave 검색 실패 시 사용할 비상 학습 콘텐츠 팩토리
 */
@Component
@RequiredArgsConstructor
public class LearningContentFallbackFactory {

    private final LearningContentTemplateFactory templateFactory;

    /**
     * 일일 콘텐츠 생성
     */
    public LearningContent createDailyContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        String title = buildTitle(studyLanguage, learningStyle, difficultyLevel);
        String sourceText = buildSourceText(studyLanguage, learningStyle);
        LearningContentTemplateFactory.ExpressionPair expressions = templateFactory.extractExpressions(studyLanguage, sourceText);

        return LearningContent.create(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                title,
                templateFactory.createSummary(explanationLanguage, learningStyle, difficultyLevel, title),
                sourceText,
                templateFactory.createExplanation(explanationLanguage, learningStyle, difficultyLevel, title),
                expressions.expressionOne(),
                expressions.expressionTwo(),
                templateFactory.createQuiz(explanationLanguage, learningStyle, title),
                null,
                publishedDate
        );
    }

    /**
     * 제목 생성
     */
    private String buildTitle(StudyLanguage studyLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        String topic = switch (learningStyle) {
            case PRACTICAL_READING -> "오늘의 짧은 읽기";
            case DAILY_CONVERSATION -> "바로 써먹는 회화";
            case TODAYS_EXPRESSION -> "기억해 둘 표현";
            case BALANCED -> "오늘의 균형 학습";
        };
        return studyLanguage.getLabel() + " " + topic + " · " + difficultyLevel.getLabel();
    }

    /**
     * 원문 생성
     */
    private String buildSourceText(StudyLanguage studyLanguage, LearningStyle learningStyle) {
        return switch (studyLanguage) {
            case ENGLISH -> switch (learningStyle) {
                case PRACTICAL_READING -> "Many learners build confidence by reading one short article before breakfast.";
                case DAILY_CONVERSATION -> "Could we start ten minutes later? I want to finish this lesson first.";
                case TODAYS_EXPRESSION -> "One step at a time can still take you far.";
                case BALANCED -> "A small daily habit often matters more than a perfect long plan.";
            };
            case JAPANESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "多くの学習者は、朝ごはんの前に短い文章を読む習慣を続けています。";
                case DAILY_CONVERSATION -> "今日は少しだけ遅く始めてもいいですか。先にこの復習を終えたいです。";
                case TODAYS_EXPRESSION -> "少しずつでも前に進めば大丈夫です。";
                case BALANCED -> "小さな習慣を毎日続けることが、上達への近道になります。";
            };
            case CHINESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "很多学习者会在早餐前先读一小段文章，让自己进入状态。";
                case DAILY_CONVERSATION -> "我们能晚十分钟开始吗？我想先把这节课复习完。";
                case TODAYS_EXPRESSION -> "一步一步来，也能走得很远。";
                case BALANCED -> "每天坚持一个小习惯，往往比做一个完美计划更重要。";
            };
        };
    }

}
