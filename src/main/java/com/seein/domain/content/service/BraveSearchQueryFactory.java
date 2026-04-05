package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Brave Search용 검색어 팩토리
 */
@Component
public class BraveSearchQueryFactory {

    /**
     * 학습 설정별 Brave 검색어 생성
     */
    public SearchQuery create(StudyLanguage studyLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, LocalDate publishedDate) {
        String query = switch (studyLanguage) {
            case ENGLISH -> englishQuery(learningStyle, difficultyLevel);
            case JAPANESE -> japaneseQuery(learningStyle, difficultyLevel);
            case CHINESE -> chineseQuery(learningStyle, difficultyLevel);
        };

        String primaryFreshness = publishedDate.equals(LocalDate.now()) ? "pd" : "pw";
        String fallbackFreshness = primaryFreshness.equals("pd") ? "pw" : "pm";
        return new SearchQuery(query, primaryFreshness, fallbackFreshness);
    }

    private String englishQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "recent English short news article " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "recent English daily conversation phrase example " + difficultyKeyword(difficultyLevel);
            case TODAYS_EXPRESSION -> "recent English useful expression idiom example " + difficultyKeyword(difficultyLevel);
            case BALANCED -> "recent English article with practical phrase " + difficultyKeyword(difficultyLevel);
        };
    }

    private String japaneseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "最近 日本語 短い 記事 " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "最近 日本語 日常会話 フレーズ " + difficultyKeyword(difficultyLevel);
            case TODAYS_EXPRESSION -> "最近 日本語 表現 例文 " + difficultyKeyword(difficultyLevel);
            case BALANCED -> "最近 日本語 記事 表現 学習 " + difficultyKeyword(difficultyLevel);
        };
    }

    private String chineseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "最新 中文 短 文 文章 " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "最新 中文 日常 会话 例句 " + difficultyKeyword(difficultyLevel);
            case TODAYS_EXPRESSION -> "最新 中文 常用 表达 例句 " + difficultyKeyword(difficultyLevel);
            case BALANCED -> "最新 中文 文章 表达 学习 " + difficultyKeyword(difficultyLevel);
        };
    }

    private String difficultyKeyword(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case BEGINNER -> "beginner";
            case INTERMEDIATE -> "intermediate";
            case ADVANCED -> "advanced";
        };
    }

    public record SearchQuery(String query, String primaryFreshness, String fallbackFreshness) {
    }
}
