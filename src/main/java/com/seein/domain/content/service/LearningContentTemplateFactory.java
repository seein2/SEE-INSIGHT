package com.seein.domain.content.service;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 학습 콘텐츠 설명/복습/표현 템플릿 팩토리
 */
@Component
public class LearningContentTemplateFactory {

    /**
     * 요약 생성
     */
    public String createSummary(ExplanationLanguage explanationLanguage, LearningStyle learningStyle,
                                DifficultyLevel difficultyLevel, String title) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> "'" + title + "'를 바탕으로 " + difficultyLevel.getLabel()
                    + " 단계에서 읽기 흐름과 핵심 표현을 빠르게 익히는 카드입니다.";
            case DAILY_CONVERSATION -> "'" + title + "'에서 바로 써먹을 수 있는 회화 표현을 중심으로 정리한 카드입니다.";
            case TODAYS_EXPRESSION -> "'" + title + "'와 연결된 핵심 표현을 짧게 익히고 바로 복습할 수 있는 카드입니다.";
            case BALANCED -> "'" + title + "'를 읽고 표현과 복습 질문까지 한 번에 연결하는 균형형 카드입니다.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "A " + difficultyLevel.getLabel().toLowerCase(Locale.ROOT)
                    + " practical reading card built around " + title + ".";
            case DAILY_CONVERSATION -> "A conversation-focused card built around phrases from " + title + ".";
            case TODAYS_EXPRESSION -> "A quick expression card connected to the topic of " + title + ".";
            case BALANCED -> "A balanced learning card that connects reading, phrases, and review around " + title + ".";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 해설 생성
     */
    public String createExplanation(ExplanationLanguage explanationLanguage, LearningStyle learningStyle,
                                    DifficultyLevel difficultyLevel, String title) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> "'" + title + "'에서는 핵심 동사와 주어 흐름을 먼저 파악하세요. "
                    + difficultyLevel.getLabel() + " 단계에서는 세부 해석보다 전체 의미를 빠르게 잡는 연습이 중요합니다.";
            case DAILY_CONVERSATION -> "'" + title + "'와 연결된 문장은 통째로 익히는 편이 효율적입니다. "
                    + "짧게 소리 내어 읽고, 주어와 상황만 바꿔 반복해 보세요.";
            case TODAYS_EXPRESSION -> "표현 학습은 뜻만 외우지 말고 어떤 장면에서 쓰이는지까지 기억해야 오래 남습니다. "
                    + "'" + title + "'의 맥락과 함께 묶어서 암기하세요.";
            case BALANCED -> "'" + title + "'를 읽은 뒤 핵심 표현 두 개를 먼저 고르고, 마지막에 복습 질문으로 내용을 다시 꺼내 보세요. "
                    + "읽기와 회화를 함께 연결하는 데 유리합니다.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "Start with the main verbs and the overall flow of " + title
                    + ". At this level, quick comprehension matters more than perfect translation.";
            case DAILY_CONVERSATION -> "Treat the phrases from " + title
                    + " as reusable chunks. Read them aloud, then reuse them in a new situation.";
            case TODAYS_EXPRESSION -> "Expressions last longer when you remember the situation as well as the meaning. "
                    + "Keep the context of " + title + " with the phrase.";
            case BALANCED -> "Read " + title
                    + ", pick two phrases, and finish with a short recall question. That sequence improves retention.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 복습 문제 생성
     */
    public String createQuiz(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, String title) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> "'" + title + "'에서 핵심 행동이나 변화를 나타내는 표현은 무엇인가요?";
            case DAILY_CONVERSATION -> "'" + title + "'의 표현을 약속, 요청, 일정 조정 상황에 맞게 다시 말해 보세요.";
            case TODAYS_EXPRESSION -> "'" + title + "'와 어울리는 상황을 하나 떠올리고 표현을 넣어 짧게 말해 보세요.";
            case BALANCED -> "'" + title + "'를 떠올리며 오늘 표현 중 하나를 사용해 짧은 문장을 만들어 보세요.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "Which phrase shows the main action or change in " + title + "?";
            case DAILY_CONVERSATION -> "Can you reuse a phrase from " + title + " in a scheduling or request situation?";
            case TODAYS_EXPRESSION -> "What situation best matches the key expression connected to " + title + "?";
            case BALANCED -> "Write one short sentence using a key phrase from the topic of " + title + ".";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    /**
     * 원문에서 표현 2개 추출
     */
    public ExpressionPair extractExpressions(StudyLanguage studyLanguage, String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return new ExpressionPair(null, null);
        }

        List<String> candidates = new ArrayList<>();
        String normalized = sourceText.replaceAll("\\s+", " ").trim();

        for (String sentence : normalized.split("[.!?。！？]")) {
            for (String segment : sentence.split("[,;:、，]")) {
                String candidate = sanitizeExpression(segment);
                if (isValidExpression(studyLanguage, candidate) && !candidates.contains(candidate)) {
                    candidates.add(candidate);
                }
            }
        }

        if (candidates.size() < 2 && studyLanguage == StudyLanguage.ENGLISH) {
            candidates.addAll(extractEnglishWordWindows(normalized, candidates));
        }

        String expressionOne = candidates.size() > 0 ? candidates.get(0) : null;
        String expressionTwo = candidates.size() > 1 ? candidates.get(1) : null;
        return new ExpressionPair(expressionOne, expressionTwo);
    }

    private List<String> extractEnglishWordWindows(String sourceText, List<String> existing) {
        List<String> expressions = new ArrayList<>();
        String cleaned = sourceText.replaceAll("[^A-Za-z0-9' ]", " ").replaceAll("\\s+", " ").trim();
        if (!StringUtils.hasText(cleaned)) {
            return expressions;
        }

        String[] words = cleaned.split(" ");
        for (int i = 0; i <= words.length - 3; i++) {
            String candidate = String.join(" ", words[i], words[i + 1], words[i + 2]).trim();
            if (candidate.length() >= 8 && candidate.length() <= 40
                    && !existing.contains(candidate) && !expressions.contains(candidate)) {
                expressions.add(candidate);
            }
            if (expressions.size() >= 2) {
                break;
            }
        }
        return expressions;
    }

    private String sanitizeExpression(String candidate) {
        return candidate
                .replaceAll("\\s+", " ")
                .replaceAll("^[\\-\\s]+", "")
                .replaceAll("[\\-\\s]+$", "")
                .trim();
    }

    private boolean isValidExpression(StudyLanguage studyLanguage, String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return false;
        }

        return switch (studyLanguage) {
            case ENGLISH -> candidate.length() >= 8 && candidate.length() <= 50 && candidate.contains(" ");
            case JAPANESE, CHINESE -> candidate.length() >= 4 && candidate.length() <= 24;
        };
    }

    private String inExplanationLanguage(ExplanationLanguage explanationLanguage, String korean, String english) {
        return explanationLanguage == ExplanationLanguage.KOREAN ? korean : english;
    }

    public record ExpressionPair(String expressionOne, String expressionTwo) {
    }
}
