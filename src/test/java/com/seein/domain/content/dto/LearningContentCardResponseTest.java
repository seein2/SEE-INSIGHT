package com.seein.domain.content.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LearningContentCardResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("콘텐츠 카드 응답은 삭제된 DB 컬럼명이 아닌 화면용 필드명으로 직렬화된다")
    void serialize_usesViewFieldNames() throws Exception {
        // given
        LearningContentCardResponse response = new LearningContentCardResponse(
                1,
                "오늘의 균형 학습",
                "원문",
                "학습 포인트",
                "표현1",
                "표현2",
                "복습 질문",
                "https://example.com",
                "ENGLISH",
                "영어",
                "KOREAN",
                "한국어",
                "BALANCED",
                "균형 학습",
                "BEGINNER",
                "초급",
                LocalDate.of(2026, 4, 25)
        );

        // when
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        // then
        assertThat(json.get("learningPointText").asText()).isEqualTo("학습 포인트");
        assertThat(json.get("reviewQuestionText").asText()).isEqualTo("복습 질문");
        assertThat(json.has("explanationText")).isFalse();
        assertThat(json.has("quizText")).isFalse();
    }
}
