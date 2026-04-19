package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 학습 콘텐츠 설명/복습/표현 템플릿 팩토리
 */
@Component
public class LearningContentTemplateFactory {

    /**
     * 요약 생성
     */
    public String createSummary(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel, String title) {
        String korean = switch (learningStyle) {
            case NEWS_READING -> "'" + title + "'를 바탕으로 " + difficultyLevel.getLabel()
                    + " 단계에서 읽기 흐름과 핵심 표현을 빠르게 익히는 카드입니다.";
            case DAILY_CONVERSATION -> "'" + title + "'에서 바로 써먹을 수 있는 회화 표현을 중심으로 정리한 카드입니다.";
            case TODAYS_EXPRESSION -> "'" + title + "'와 연결된 핵심 표현을 짧게 익히고 바로 복습할 수 있는 카드입니다.";
            case BALANCED -> "'" + title + "'를 읽고 표현과 복습 질문까지 한 번에 연결하는 균형형 카드입니다.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> "A " + difficultyLevel.getLabel().toLowerCase(Locale.ROOT)
                    + " practical reading card built around " + title + ".";
            case DAILY_CONVERSATION -> "A conversation-focused card built around phrases from " + title + ".";
            case TODAYS_EXPRESSION -> "A quick expression card connected to the topic of " + title + ".";
            case BALANCED -> "A balanced learning card that connects reading, phrases, and review around " + title + ".";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 학습 포인트 생성
     */
    public String createExplanation(ExplanationLanguage explanationLanguage, LearningStyle learningStyle,
                                    DifficultyLevel difficultyLevel, String title, String sourceText) {
        String safeSourceText = sourceText == null ? "" : sourceText;
        String sourcePreview = safeSourceText.length() > 90 ? safeSourceText.substring(0, 90).trim() + "..." : safeSourceText;
        String korean = switch (learningStyle) {
            case NEWS_READING -> "이 콘텐츠는 실제 뉴스 문맥에서 가져온 짧은 원문입니다. 먼저 누가 무엇을 했는지 잡고, "
                    + "'" + sourcePreview + "'의 핵심 정보를 한 문장으로 다시 말해 보세요.";
            case DAILY_CONVERSATION -> "이 문장은 실제 상황에서 통째로 기억하기 좋습니다. 주어와 장소만 바꿔 소리 내어 반복해 보세요.";
            case TODAYS_EXPRESSION -> "표현은 뜻보다 사용 장면이 중요합니다. '" + sourcePreview
                    + "'에서 표현이 어떤 분위기와 의도로 쓰였는지 함께 기억하세요.";
            case BALANCED -> "짧은 원문을 읽고 핵심 의미를 잡은 뒤, 눈에 띄는 표현 하나를 골라 직접 문장으로 바꿔 보세요.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> "This short excerpt comes from a real news context. Find who did what, then restate the core point in one sentence.";
            case DAILY_CONVERSATION -> "Treat this sentence as a reusable chunk. Read it aloud and swap the subject or situation.";
            case TODAYS_EXPRESSION -> "Expressions stick when you remember the situation. Notice how the phrase works in this context: " + sourcePreview;
            case BALANCED -> "Read the excerpt, capture the main meaning, then reuse one phrase in your own sentence.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 문제 생성
     */
    public String createQuiz(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, String title) {
        String korean = switch (learningStyle) {
            case NEWS_READING -> "'" + title + "'에서 핵심 행동이나 변화를 나타내는 표현은 무엇인가요?";
            case DAILY_CONVERSATION -> "'" + title + "'의 표현을 약속, 요청, 일정 조정 상황에 맞게 다시 말해 보세요.";
            case TODAYS_EXPRESSION -> "'" + title + "'와 어울리는 상황을 하나 떠올리고 표현을 넣어 짧게 말해 보세요.";
            case BALANCED -> "'" + title + "'를 떠올리며 오늘 표현 중 하나를 사용해 짧은 문장을 만들어 보세요.";
        };
        String english = switch (learningStyle) {
            case NEWS_READING -> "Which phrase shows the main action or change in " + title + "?";
            case DAILY_CONVERSATION -> "Can you reuse a phrase from " + title + " in a scheduling or request situation?";
            case TODAYS_EXPRESSION -> "What situation best matches the key expression connected to " + title + "?";
            case BALANCED -> "Write one short sentence using a key phrase from the topic of " + title + ".";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
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
