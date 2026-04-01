package com.seein.domain.content.service;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부 API 실패 시 사용할 기본 학습 콘텐츠 팩토리
 */
@Component
public class LearningContentFallbackFactory {

    public LearningContent createDailyContent(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalDate publishedDate
    ) {
        return LearningContent.create(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                buildTitle(studyLanguage, learningStyle, difficultyLevel),
                buildSummary(explanationLanguage, learningStyle, difficultyLevel),
                buildSourceText(studyLanguage, learningStyle),
                buildExplanation(explanationLanguage, learningStyle, difficultyLevel),
                buildExpressionOne(studyLanguage, learningStyle),
                buildExpressionTwo(studyLanguage, learningStyle),
                buildQuiz(explanationLanguage, learningStyle),
                "https://www.perplexity.ai/",
                publishedDate
        );
    }

    public List<LearningContent> createFeedContents(StudyLanguage studyLanguage, ExplanationLanguage explanationLanguage) {
        LocalDate publishedDate = LocalDate.now();
        List<LearningContent> contents = new ArrayList<>();
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.BALANCED, DifficultyLevel.INTERMEDIATE, publishedDate));
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.PRACTICAL_READING, DifficultyLevel.BEGINNER, publishedDate));
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.DAILY_CONVERSATION, DifficultyLevel.BEGINNER, publishedDate));
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.TODAYS_EXPRESSION, DifficultyLevel.INTERMEDIATE, publishedDate));
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.PRACTICAL_READING, DifficultyLevel.ADVANCED, publishedDate.minusDays(1)));
        contents.add(createDailyContent(studyLanguage, explanationLanguage, LearningStyle.BALANCED, DifficultyLevel.BEGINNER, publishedDate.minusDays(1)));
        return contents;
    }

    private String buildTitle(StudyLanguage studyLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        String topic = switch (learningStyle) {
            case PRACTICAL_READING -> "오늘의 짧은 읽기";
            case DAILY_CONVERSATION -> "바로 써먹는 회화";
            case TODAYS_EXPRESSION -> "기억해 둘 표현";
            case BALANCED -> "오늘의 균형 학습";
        };
        return studyLanguage.getLabel() + " " + topic + " · " + difficultyLevel.getLabel();
    }

    private String buildSummary(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> difficultyLevel.getLabel() + " 수준에 맞춘 짧은 실전 문장을 읽고 핵심 흐름을 익히는 카드입니다.";
            case DAILY_CONVERSATION -> "일상에서 바로 꺼내 쓸 수 있는 회화 표현을 자연스럽게 익히는 카드입니다.";
            case TODAYS_EXPRESSION -> "한 번 보면 오래 기억에 남는 표현과 예문을 함께 정리한 카드입니다.";
            case BALANCED -> "읽기, 회화, 표현을 한 번에 연결해 주는 균형형 학습 카드입니다.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "A short practical reading card tuned to the selected level.";
            case DAILY_CONVERSATION -> "A quick conversation card for phrases you can reuse today.";
            case TODAYS_EXPRESSION -> "A focused expression card with meaning and usage.";
            case BALANCED -> "A balanced card that connects reading, expression, and review.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

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

    private String buildExplanation(ExplanationLanguage explanationLanguage, LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> "짧은 정보성 문장을 읽으면서 핵심 동사와 문장 흐름을 확인하세요. " + difficultyLevel.getLabel() + " 단계에서는 부담 없이 전체 의미를 먼저 잡는 것이 중요합니다.";
            case DAILY_CONVERSATION -> "일상 회화에서는 짧고 자연스러운 요청 표현을 통째로 익히는 편이 빠릅니다. 먼저 리듬을 따라 읽고, 상황을 바꿔서 반복해 보세요.";
            case TODAYS_EXPRESSION -> "표현은 뜻만 외우기보다 어떤 상황에서 쓰는지까지 함께 기억해야 오래 남습니다. 예문과 함께 묶어서 암기하세요.";
            case BALANCED -> "읽기와 표현, 복습 질문을 한 번에 연결하면 기억 유지에 유리합니다. 오늘은 전체 의미를 이해한 뒤 표현 두 개를 소리 내어 반복해 보세요.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "Focus on the core verbs and the overall flow first. At this level, quick comprehension matters more than perfect translation.";
            case DAILY_CONVERSATION -> "For daily conversation, memorize the phrase as one chunk, then repeat it in two or three new situations.";
            case TODAYS_EXPRESSION -> "Expressions last longer when you remember the situation, not just the meaning. Keep the example sentence with it.";
            case BALANCED -> "Connecting reading, explanation, and review in one pass improves retention. Understand the whole message first, then repeat the key phrases aloud.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    private String buildExpressionOne(StudyLanguage studyLanguage, LearningStyle learningStyle) {
        return switch (studyLanguage) {
            case ENGLISH -> switch (learningStyle) {
                case PRACTICAL_READING -> "build confidence";
                case DAILY_CONVERSATION -> "start ten minutes later";
                case TODAYS_EXPRESSION -> "one step at a time";
                case BALANCED -> "small daily habit";
            };
            case JAPANESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "習慣を続ける";
                case DAILY_CONVERSATION -> "少しだけ遅く";
                case TODAYS_EXPRESSION -> "少しずつ";
                case BALANCED -> "毎日続ける";
            };
            case CHINESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "进入状态";
                case DAILY_CONVERSATION -> "晚十分钟开始";
                case TODAYS_EXPRESSION -> "一步一步来";
                case BALANCED -> "每天坚持";
            };
        };
    }

    private String buildExpressionTwo(StudyLanguage studyLanguage, LearningStyle learningStyle) {
        return switch (studyLanguage) {
            case ENGLISH -> switch (learningStyle) {
                case PRACTICAL_READING -> "short article";
                case DAILY_CONVERSATION -> "finish this lesson";
                case TODAYS_EXPRESSION -> "take you far";
                case BALANCED -> "perfect long plan";
            };
            case JAPANESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "短い文章";
                case DAILY_CONVERSATION -> "復習を終える";
                case TODAYS_EXPRESSION -> "前に進む";
                case BALANCED -> "上達への近道";
            };
            case CHINESE -> switch (learningStyle) {
                case PRACTICAL_READING -> "一小段文章";
                case DAILY_CONVERSATION -> "复习完";
                case TODAYS_EXPRESSION -> "走得很远";
                case BALANCED -> "完美计划";
            };
        };
    }

    private String buildQuiz(ExplanationLanguage explanationLanguage, LearningStyle learningStyle) {
        String korean = switch (learningStyle) {
            case PRACTICAL_READING -> "오늘 문장에서 핵심 행동을 나타내는 동사는 무엇인가요?";
            case DAILY_CONVERSATION -> "이 표현을 약속 시간 조정 상황에 맞게 다시 말해 보세요.";
            case TODAYS_EXPRESSION -> "이 표현이 어울리는 상황을 하나 떠올려 보세요.";
            case BALANCED -> "오늘 표현 두 개 중 하나를 사용해 짧은 문장을 만들어 보세요.";
        };
        String english = switch (learningStyle) {
            case PRACTICAL_READING -> "Which verb shows the main action in today's sentence?";
            case DAILY_CONVERSATION -> "Can you reuse this phrase to change the time of a meeting?";
            case TODAYS_EXPRESSION -> "What kind of situation matches this expression?";
            case BALANCED -> "Write one short sentence using one of today's key phrases.";
        };
        return inExplanationLanguage(explanationLanguage, korean, english);
    }

    private String inExplanationLanguage(ExplanationLanguage explanationLanguage, String korean, String english) {
        return explanationLanguage == ExplanationLanguage.KOREAN ? korean : english;
    }
}
