package com.seein.domain.content.service;

import com.seein.domain.content.entity.ContentSourceType;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BraveSearchQueryFactoryTest {

    private final BraveSearchQueryFactory queryFactory = new BraveSearchQueryFactory();

    @Test
    @DisplayName("뉴스 읽기는 news/search와 지역/언어/freshness를 사용한다")
    void create_practicalReadingUsesNewsSearchProfile() {
        // when
        BraveSearchQueryFactory.SearchQuery query = queryFactory.create(
                StudyLanguage.ENGLISH,
                LearningStyle.NEWS_READING,
                DifficultyLevel.BEGINNER,
                LocalDate.now()
        );

        // then
        assertThat(query.sourceType()).isEqualTo(ContentSourceType.NEWS);
        assertThat(query.country()).isEqualTo("US");
        assertThat(query.searchLanguage()).isEqualTo("en");
        assertThat(query.uiLanguage()).isEqualTo("en-US");
        assertThat(query.primaryFreshness()).isEqualTo("pd");
        assertThat(query.fallbackFreshness()).isEqualTo("pw");
        assertThat(query.useLlmContext()).isFalse();
        assertThat(query.query()).contains("A1 absolute beginner", "simple words");
    }

    @Test
    @DisplayName("오늘의 표현은 최신성 조건 없이 web/search와 LLM Context를 사용한다")
    void create_todaysExpressionUsesEvergreenProfile() {
        // when
        BraveSearchQueryFactory.SearchQuery query = queryFactory.create(
                StudyLanguage.JAPANESE,
                LearningStyle.TODAYS_EXPRESSION,
                DifficultyLevel.BEGINNER,
                LocalDate.now()
        );

        // then
        assertThat(query.sourceType()).isEqualTo(ContentSourceType.WEB);
        assertThat(query.country()).isEqualTo("JP");
        assertThat(query.searchLanguage()).isEqualTo("jp");
        assertThat(query.uiLanguage()).isEqualTo("ja-JP");
        assertThat(query.primaryFreshness()).isNull();
        assertThat(query.fallbackFreshness()).isNull();
        assertThat(query.useLlmContext()).isTrue();
        assertThat(query.query()).contains("初級", "N5", "初心者");
    }

    @Test
    @DisplayName("균형 학습은 최근 1주 조회 후 최근 1개월로 fallback한다")
    void create_balancedUsesWeeklyWithMonthlyFallback() {
        // when
        BraveSearchQueryFactory.SearchQuery query = queryFactory.create(
                StudyLanguage.CHINESE,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalDate.of(2026, 4, 10)
        );

        // then
        assertThat(query.sourceType()).isEqualTo(ContentSourceType.WEB);
        assertThat(query.country()).isEqualTo("CN");
        assertThat(query.searchLanguage()).isEqualTo("zh-hans");
        assertThat(query.uiLanguage()).isEqualTo("zh-CN");
        assertThat(query.primaryFreshness()).isEqualTo("pw");
        assertThat(query.fallbackFreshness()).isEqualTo("pm");
        assertThat(query.useLlmContext()).isTrue();
        assertThat(query.query()).contains("零基础", "HSK 1", "初学者", "拼音");
    }
}
