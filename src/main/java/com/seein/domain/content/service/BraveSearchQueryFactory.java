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
        DifficultyLevel level = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        String query = switch (studyLanguage) {
            case ENGLISH -> englishQuery(learningStyle, level);
            case JAPANESE -> japaneseQuery(learningStyle, level);
            case CHINESE -> chineseQuery(learningStyle, level);
        };

        // 학습 스타일과 콘텐츠 신선도 기준에 따른 검색어 옵션 결정
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
            case NEWS_READING -> englishDifficultyKeyword(difficultyLevel) + " English learners short local news article simple words";
            case DAILY_CONVERSATION -> englishDifficultyKeyword(difficultyLevel) + " English daily conversation phrase short example";
            case TODAYS_EXPRESSION -> englishDifficultyKeyword(difficultyLevel) + " English common expression phrase simple meaning example";
            case BALANCED -> englishDifficultyKeyword(difficultyLevel) + " English short reading practical phrase simple learning";
        };
    }

    private String japaneseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case NEWS_READING -> "やさしい日本語 ニュース 短い 記事 " + japaneseDifficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "日本語 日常会話 短い 例文 " + japaneseDifficultyKeyword(difficultyLevel);
            case TODAYS_EXPRESSION -> "日本語 よく使う 表現 短い 例文 意味 " + japaneseDifficultyKeyword(difficultyLevel);
            case BALANCED -> "日本語 やさしい 短文 表現 学習 " + japaneseDifficultyKeyword(difficultyLevel);
        };
    }

    private String chineseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case NEWS_READING -> "简单中文 新闻 短文 " + chineseDifficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "中文 日常会话 简单 例句 " + chineseDifficultyKeyword(difficultyLevel);
            case TODAYS_EXPRESSION -> "中文 常用表达 简单 例句 意思 " + chineseDifficultyKeyword(difficultyLevel);
            case BALANCED -> "简单中文 短文 表达 学习 " + chineseDifficultyKeyword(difficultyLevel);
        };
    }

    private String englishDifficultyKeyword(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case BEGINNER -> "A1 absolute beginner";
            case INTERMEDIATE -> "B1 intermediate";
            case ADVANCED -> "advanced";
        };
    }

    private String japaneseDifficultyKeyword(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case BEGINNER -> "初級 N5 初心者";
            case INTERMEDIATE -> "中級 N3";
            case ADVANCED -> "上級 自然な表現";
        };
    }

    private String chineseDifficultyKeyword(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case BEGINNER -> "零基础 HSK 1 初学者 拼音";
            case INTERMEDIATE -> "HSK 3 中级";
            case ADVANCED -> "HSK 5 高级 自然表达";
        };
    }

    // 학습 스타일에 따른 콘텐츠 출처 유형 결정
    private ContentSourceType sourceType(LearningStyle learningStyle) {
        return learningStyle == LearningStyle.NEWS_READING ? ContentSourceType.NEWS : ContentSourceType.WEB;
    }

    // 뉴스는 LLM 컨텍스트를 사용하지 않음
    private boolean useLlmContext(LearningStyle learningStyle) {
        return learningStyle != LearningStyle.NEWS_READING;
    }

    private String primaryFreshness(LearningStyle learningStyle, LocalDate publishedDate) {
        if (learningStyle == LearningStyle.TODAYS_EXPRESSION) {
            return null;
        }

        // 뉴스는 오늘 발행된 콘텐츠를 우선적으로 검색
        if (learningStyle == LearningStyle.NEWS_READING) {
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
