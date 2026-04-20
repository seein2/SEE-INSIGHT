package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import org.springframework.stereotype.Component;

/**
 * 학습 콘텐츠 설명/복습/표현 템플릿 팩토리
 */
@Component
public class LearningContentTemplateFactory {

    /**
     * 요약 생성
     */
    public String createSummary(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        DifficultyLevel level = normalizeDifficultyLevel(difficultyLevel);
        String korean = switch (learningStyle) {
            case NEWS_READING -> beginner(level)
                    ? "짧은 원문에서 사람·행동·시간 표현을 찾아 읽습니다."
                    : "원문 흐름을 잡고 핵심 표현을 하나 골라 읽습니다.";
            case DAILY_CONVERSATION -> beginner(level)
                    ? "짧은 회화 문장을 따라 읽고 한 단어만 바꿔 말합니다."
                    : "일상 상황에서 바로 바꿔 쓸 수 있는 문장 구조를 익힙니다.";
            case TODAYS_EXPRESSION -> beginner(level)
                    ? "자주 쓰는 표현 하나를 짧은 상황과 함께 기억합니다."
                    : "표현의 뜻보다 쓰이는 장면을 먼저 익힙니다.";
            case BALANCED -> beginner(level)
                    ? "짧게 읽고 표현 하나를 골라 내 문장으로 바꿔 봅니다."
                    : "읽기, 표현 선택, 짧은 복습을 한 번에 연결합니다.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> beginner(level)
                    ? "Find the person, action, and time in one short excerpt."
                    : "Follow the excerpt and pick one useful phrase.";
            case DAILY_CONVERSATION -> beginner(level)
                    ? "Repeat one short sentence and change just one word."
                    : "Practice a sentence pattern you can reuse in daily conversation.";
            case TODAYS_EXPRESSION -> beginner(level)
                    ? "Remember one common expression with a simple situation."
                    : "Learn when to use the expression, not just what it means.";
            case BALANCED -> beginner(level)
                    ? "Read briefly, pick one phrase, and make your own sentence."
                    : "Connect reading, phrase choice, and quick review.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 학습 포인트 생성
     */
    public String createExplanation(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        DifficultyLevel level = normalizeDifficultyLevel(difficultyLevel);
        String korean = switch (learningStyle) {
            case NEWS_READING -> beginner(level)
                    ? "전체를 번역하지 말고 사람·기관, 행동, 시간 표현만 먼저 표시하세요."
                    : "핵심 행동을 나타내는 동사를 찾고, 그 행동이 왜 중요한지 한 문장으로 정리하세요.";
            case DAILY_CONVERSATION -> beginner(level)
                    ? "문장을 통째로 따라 읽은 뒤 이름, 시간, 장소 중 하나만 바꿔 다시 말하세요."
                    : "상황은 유지하고 주어와 목적어를 바꿔 같은 문장 구조를 반복하세요.";
            case TODAYS_EXPRESSION -> beginner(level)
                    ? "표현 앞뒤의 쉬운 단어를 같이 보세요. 뜻보다 언제 쓰는지 먼저 기억하세요."
                    : "표현이 나온 상황과 말하는 사람의 의도를 함께 묶어 기억하세요.";
            case BALANCED -> beginner(level)
                    ? "모르는 단어를 모두 해석하지 말고, 바로 따라 쓸 수 있는 짧은 표현 하나만 고르세요."
                    : "본문의 핵심 의미를 잡은 뒤, 같은 구조로 내 문장 하나를 만드세요.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> beginner(level)
                    ? "Do not translate everything. Mark only the person, action, and time first."
                    : "Find the main verb, then explain why that action matters in one sentence.";
            case DAILY_CONVERSATION -> beginner(level)
                    ? "Read the whole sentence aloud, then change one name, time, or place."
                    : "Keep the situation and swap the subject or object to reuse the pattern.";
            case TODAYS_EXPRESSION -> beginner(level)
                    ? "Look at the easy words around the expression. Remember when to use it first."
                    : "Connect the expression with the situation and the speaker's intention.";
            case BALANCED -> beginner(level)
                    ? "Skip difficult words for now. Choose one short phrase you can reuse today."
                    : "Capture the main meaning, then make one new sentence with the same pattern.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 문제 생성
     */
    public String createQuiz(ExplanationLanguage explanationLanguage, LearningStyle learningStyle) {
        String korean = switch (learningStyle) {
            case NEWS_READING -> "원문에서 사람·기관과 행동을 각각 하나씩 찾아 적어보세요.";
            case DAILY_CONVERSATION -> "같은 문장 구조로 오늘 실제로 말할 수 있는 한 문장을 만들어 보세요.";
            case TODAYS_EXPRESSION -> "이 표현을 쓸 수 있는 내 상황을 하나 정하고 짧게 말해 보세요.";
            case BALANCED -> "오늘 고른 표현 하나로 새 문장을 짧게 만들어 보세요.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> "Find one person or group and one action in the excerpt.";
            case DAILY_CONVERSATION -> "Use the same sentence pattern to say something real today.";
            case TODAYS_EXPRESSION -> "Choose one situation where you could use this expression.";
            case BALANCED -> "Make one short new sentence with the phrase you picked today.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /*
     * 난이도가 없으면 서비스 기본값인 초급으로 처리
     */
    private DifficultyLevel normalizeDifficultyLevel(DifficultyLevel difficultyLevel) {
        return difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
    }

    /*
     * 초급자는 해석보다 문장 구조와 반복 행동을 먼저 잡도록 안내
     */
    private boolean beginner(DifficultyLevel difficultyLevel) {
        return difficultyLevel == DifficultyLevel.BEGINNER;
    }

    /*
     * 설명 언어에 따라 한국어 또는 영어 텍스트 반환
     */
    private String inExplanationLanguage(ExplanationLanguage explanationLanguage, String korean, String english) {
        return explanationLanguage == ExplanationLanguage.KOREAN ? korean : english;
    }

    public record ExpressionPair(String expressionOne, String expressionTwo) {
    }
}
