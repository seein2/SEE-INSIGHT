package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
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
        DifficultyLevel effectiveDifficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        String query = switch (studyLanguage) {
            case ENGLISH -> englishQuery(learningStyle, effectiveDifficultyLevel);
            case JAPANESE -> japaneseQuery(learningStyle, effectiveDifficultyLevel);
            case CHINESE -> chineseQuery(learningStyle, effectiveDifficultyLevel);
        };

        String primaryFreshness = primaryFreshness(learningStyle, publishedDate);
        String fallbackFreshness = fallbackFreshness(learningStyle, primaryFreshness);
        return new SearchQuery(
                query,
                studyLanguage.getSearchCountryCode(),
                studyLanguage.getSearchLanguageCode(),
                studyLanguage.getUiLanguageCode(),
                sourceType(learningStyle),
                primaryFreshness,
                fallbackFreshness,
                useLlmContext(learningStyle),
                8
        );
    }

    private String englishQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "short local news article English learners " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "common English daily conversation phrase example";
            case TODAYS_EXPRESSION -> "useful English expression idiom example meaning";
            case BALANCED -> "English article practical phrase short learning";
        };
    }

    private String japaneseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "日本 ニュース 短い 記事 " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "日本語 日常会話 フレーズ 例文";
            case TODAYS_EXPRESSION -> "日本語 よく使う 表現 例文 意味";
            case BALANCED -> "日本語 記事 表現 学習";
        };
    }

    private String chineseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case PRACTICAL_READING -> "中国 新闻 短 文 文章 " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "中文 日常 会话 例句";
            case TODAYS_EXPRESSION -> "中文 常用 表达 例句 意思";
            case BALANCED -> "中文 文章 表达 学习";
        };
    }

    private String difficultyKeyword(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case BEGINNER -> "beginner";
            case INTERMEDIATE -> "intermediate";
            case ADVANCED -> "advanced";
        };
    }

    private ContentSourceType sourceType(LearningStyle learningStyle) {
        return learningStyle == LearningStyle.PRACTICAL_READING ? ContentSourceType.NEWS : ContentSourceType.WEB;
    }

    private boolean useLlmContext(LearningStyle learningStyle) {
        return learningStyle != LearningStyle.PRACTICAL_READING;
    }

    private String primaryFreshness(LearningStyle learningStyle, LocalDate publishedDate) {
        if (learningStyle == LearningStyle.TODAYS_EXPRESSION) {
            return null;
        }

        if (learningStyle == LearningStyle.PRACTICAL_READING) {
            return publishedDate != null && publishedDate.equals(LocalDate.now()) ? "pd" : "pw";
        }

        return "pw";
    }

    private String fallbackFreshness(LearningStyle learningStyle, String primaryFreshness) {
        if (learningStyle == LearningStyle.TODAYS_EXPRESSION) {
            return null;
        }

        return "pd".equals(primaryFreshness) ? "pw" : "pm";
    }

    public record SearchQuery(
            String query,
            String country,
            String searchLanguage,
            String uiLanguage,
            ContentSourceType sourceType,
            String primaryFreshness,
            String fallbackFreshness,
            boolean useLlmContext,
            int count
    ) {
    }
}
