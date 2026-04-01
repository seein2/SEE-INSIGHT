package com.seein.domain.subscription.repository;

import com.seein.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 구독 Repository
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    /**
     * 회원의 구독 목록 조회
     */
    List<Subscription> findByMemberMemberIdOrderByCreatedAtDesc(Integer memberId);

    /**
     * 회원 + 구독 ID로 구독 조회
     */
    Optional<Subscription> findByMemberMemberIdAndSubscriptionId(Integer memberId, Integer subscriptionId);

    /**
     * 회원의 전체 구독 수
     */
    long countByMemberMemberId(Integer memberId);

    /**
     * 동일 학습 설정 구독 존재 여부
     */
    boolean existsByMemberMemberIdAndStudyLanguageAndExplanationLanguageAndLearningStyleAndDifficultyLevelAndDeliveryTime(
            Integer memberId,
            com.seein.domain.subscription.entity.StudyLanguage studyLanguage,
            com.seein.domain.subscription.entity.ExplanationLanguage explanationLanguage,
            com.seein.domain.subscription.entity.LearningStyle learningStyle,
            com.seein.domain.subscription.entity.DifficultyLevel difficultyLevel,
            LocalTime deliveryTime
    );

    /**
     * 발송 시각에 맞는 활성 구독 조회
     */
    @Query("""
            SELECT s
            FROM Subscription s
            JOIN s.member m
            WHERE s.isActive = true
              AND m.deletedAt IS NULL
              AND s.deliveryTime = :deliveryTime
            ORDER BY s.subscriptionId ASC
            """)
    List<Subscription> findDeliverableSubscriptions(LocalTime deliveryTime);
}
