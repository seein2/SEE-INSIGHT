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
            case NEWS_READING -> "short local news article English learners " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "common English daily conversation phrase example";
            case TODAYS_EXPRESSION -> "useful English expression idiom example meaning";
            case BALANCED -> "English article practical phrase short learning";
        };
    }

    private String japaneseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case NEWS_READING -> "日本 ニュース 短い 記事 " + difficultyKeyword(difficultyLevel);
            case DAILY_CONVERSATION -> "日本語 日常会話 フレーズ 例文";
            case TODAYS_EXPRESSION -> "日本語 よく使う 表現 例文 意味";
            case BALANCED -> "日本語 記事 表現 学習";
        };
    }

    private String chineseQuery(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        return switch (learningStyle) {
            case NEWS_READING -> "中国 新闻 短 文 文章 " + difficultyKeyword(difficultyLevel);
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
