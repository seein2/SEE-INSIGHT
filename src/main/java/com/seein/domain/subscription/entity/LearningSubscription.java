package com.seein.domain.subscription.entity;

import com.seein.domain.member.entity.Member;
import com.seein.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;

/**
 * 학습 이메일 구독 엔티티
 */
@Entity
@Table(
        name = "learning_subscription",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "learning_subscription_unique_settings",
                        columnNames = {
                                "member_id",
                                "study_language",
                                "explanation_language",
                                "learning_style",
                                "difficulty_level",
                                "delivery_time"
                        }
                )
        }
)
@Getter
@ToString(exclude = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningSubscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_language", nullable = false, length = 30)
    private StudyLanguage studyLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "explanation_language", nullable = false, length = 30)
    private ExplanationLanguage explanationLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style", nullable = false, length = 30)
    private LearningStyle learningStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 30)
    private DifficultyLevel difficultyLevel;

    @Column(name = "delivery_time", nullable = false)
    private LocalTime deliveryTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public static LearningSubscription create(
            Member member,
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalTime deliveryTime
    ) {
        LearningSubscription subscription = new LearningSubscription();
        subscription.member = member;
        subscription.studyLanguage = studyLanguage;
        subscription.explanationLanguage = explanationLanguage;
        subscription.learningStyle = learningStyle;
        subscription.difficultyLevel = difficultyLevel;
        subscription.deliveryTime = deliveryTime;
        subscription.isActive = true;
        return subscription;
    }

    public void updateSettings(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalTime deliveryTime
    ) {
        this.studyLanguage = studyLanguage;
        this.explanationLanguage = explanationLanguage;
        this.learningStyle = learningStyle;
        this.difficultyLevel = difficultyLevel;
        this.deliveryTime = deliveryTime;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
