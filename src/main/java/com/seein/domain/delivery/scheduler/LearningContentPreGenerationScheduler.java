package com.seein.domain.delivery.scheduler;

import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.LearningSubscription;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 홈 피드/활성 구독용 일일 콘텐츠 선생성 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningContentPreGenerationScheduler {

    private static final List<HomePreset> HOME_PRESETS = List.of(
            new HomePreset(LearningStyle.BALANCED, DifficultyLevel.INTERMEDIATE),
            new HomePreset(LearningStyle.BALANCED, DifficultyLevel.BEGINNER),
            new HomePreset(LearningStyle.PRACTICAL_READING, DifficultyLevel.BEGINNER),
            new HomePreset(LearningStyle.PRACTICAL_READING, DifficultyLevel.ADVANCED),
            new HomePreset(LearningStyle.DAILY_CONVERSATION, DifficultyLevel.BEGINNER),
            new HomePreset(LearningStyle.TODAYS_EXPRESSION, DifficultyLevel.INTERMEDIATE)
    );

    private final LearningContentService learningContentService;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 매일 새벽 홈 피드와 활성 구독 조합을 미리 생성한다.
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void preGenerateDailyLearningContent() {
        preGenerateDailyLearningContent(LocalDate.now());
    }

    /*
     * 홈 피드용 프리셋과 활성 구독 조합을 모두 모아서 일일 콘텐츠를 선생성한다.
     */
    void preGenerateDailyLearningContent(LocalDate publishedDate) {
        LinkedHashSet<GenerationTarget> targets = new LinkedHashSet<>();

        // 홈 피드용 프리셋 조합 추가
        for (StudyLanguage studyLanguage : StudyLanguage.values()) {
            for (HomePreset preset : HOME_PRESETS) {
                targets.add(new GenerationTarget(
                        studyLanguage,
                        ExplanationLanguage.KOREAN,
                        preset.learningStyle(),
                        preset.difficultyLevel()
                ));
            }
        }

        // 활성 구독 조합 추가
        for (LearningSubscription subscription : subscriptionRepository.findActiveSubscriptions()) {
            targets.add(new GenerationTarget(
                    subscription.getStudyLanguage(),
                    subscription.getExplanationLanguage(),
                    subscription.getLearningStyle(),
                    subscription.getDifficultyLevel()
            ));
        }

        // 일일 콘텐츠 선생성
        for (GenerationTarget target : targets) {
            try {
                learningContentService.getOrCreateDailyContent(
                        target.studyLanguage(),
                        target.explanationLanguage(),
                        target.learningStyle(),
                        target.difficultyLevel(),
                        publishedDate
                );
            } catch (Exception e) {
                log.error("학습 콘텐츠 선생성 실패 - studyLanguage={}, explanationLanguage={}, style={}, difficulty={}, publishedDate={}, error={}",
                        target.studyLanguage(), target.explanationLanguage(), target.learningStyle(),
                        target.difficultyLevel(), publishedDate, e.getMessage(), e);
            }
        }
    }

    /*
     * 홈 피드용 프리셋 조합 레코드
     */
    private record HomePreset(LearningStyle learningStyle, DifficultyLevel difficultyLevel) {
    }

    /*
     * 생성 조합을 레코드
     */
    private record GenerationTarget(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel
    ) {
    }
}
