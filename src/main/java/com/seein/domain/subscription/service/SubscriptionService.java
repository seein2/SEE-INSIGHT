package com.seein.domain.subscription.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.member.entity.Membership;
import com.seein.domain.subscription.dto.SubscriptionCreateRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewResponse;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.dto.SubscriptionUpdateRequest;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 구독 서비스
 * 학습 설정 기반 이메일 구독 생성/조회/수정/삭제를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final LearningContentService learningContentService;

    /**
     * 내 구독 목록 조회
     */
    public List<SubscriptionResponse> getSubscriptions(Integer memberId) {
        return subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    /**
     * 학습 이메일 구독 생성
     */
    @Transactional
    public SubscriptionResponse subscribe(Integer memberId, SubscriptionCreateRequest request) {
        Member member = findMemberById(memberId);
        validateSubscriptionLimit(member);
        validateDuplicateSubscription(memberId,
                request.getStudyLanguage(),
                request.getExplanationLanguage(),
                request.getLearningStyle(),
                request.getDifficultyLevel(),
                request.getDeliveryTime(),
                null
        );

        Subscription subscription = Subscription.create(
                member,
                request.getStudyLanguage(),
                request.getExplanationLanguage(),
                request.getLearningStyle(),
                request.getDifficultyLevel(),
                request.getDeliveryTime()
        );
        subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(subscription);
    }

    /**
     * 구독 상세 조회
     */
    public SubscriptionResponse getSubscription(Integer memberId, Integer subscriptionId) {
        Subscription subscription = findOwnedSubscription(memberId, subscriptionId);
        return SubscriptionResponse.from(subscription);
    }

    /**
     * 구독 미리보기 생성
     */
    @Transactional
    public SubscriptionPreviewResponse preview(Integer memberId, SubscriptionPreviewRequest request) {
        findMemberById(memberId);
        LearningContentCardResponse previewContent = learningContentService.getPreviewContent(
                request.getStudyLanguage(),
                request.getExplanationLanguage(),
                request.getLearningStyle(),
                request.getDifficultyLevel()
        );

        return new SubscriptionPreviewResponse(
                request.getStudyLanguage().name(),
                request.getStudyLanguage().getLabel(),
                request.getExplanationLanguage().name(),
                request.getExplanationLanguage().getLabel(),
                request.getLearningStyle().name(),
                request.getLearningStyle().getLabel(),
                request.getDifficultyLevel().name(),
                request.getDifficultyLevel().getLabel(),
                formatTime(request.getDeliveryTime()),
                "매일 " + formatTime(request.getDeliveryTime()),
                previewContent
        );
    }

    /**
     * 구독 설정 변경
     */
    @Transactional
    public SubscriptionResponse updateSubscription(Integer memberId, Integer subscriptionId, SubscriptionUpdateRequest request) {
        Subscription subscription = findOwnedSubscription(memberId, subscriptionId);
        LocalTime nextDeliveryTime = request.getDeliveryTime() != null ? request.getDeliveryTime() : subscription.getDeliveryTime();
        var nextStudyLanguage = request.getStudyLanguage() != null ? request.getStudyLanguage() : subscription.getStudyLanguage();
        var nextExplanationLanguage = request.getExplanationLanguage() != null ? request.getExplanationLanguage() : subscription.getExplanationLanguage();
        var nextLearningStyle = request.getLearningStyle() != null ? request.getLearningStyle() : subscription.getLearningStyle();
        var nextDifficultyLevel = request.getDifficultyLevel() != null ? request.getDifficultyLevel() : subscription.getDifficultyLevel();

        validateDuplicateSubscription(
                memberId,
                nextStudyLanguage,
                nextExplanationLanguage,
                nextLearningStyle,
                nextDifficultyLevel,
                nextDeliveryTime,
                subscriptionId
        );

        subscription.updateSettings(
                nextStudyLanguage,
                nextExplanationLanguage,
                nextLearningStyle,
                nextDifficultyLevel,
                nextDeliveryTime
        );

        if (request.getIsActive() != null) {
            if (request.getIsActive()) {
                subscription.activate();
            } else {
                subscription.deactivate();
            }
        }

        return SubscriptionResponse.from(subscription);
    }

    /**
     * 구독 취소 (삭제)
     */
    @Transactional
    public void unsubscribe(Integer memberId, Integer subscriptionId) {
        Subscription subscription = findOwnedSubscription(memberId, subscriptionId);
        subscriptionRepository.delete(subscription);
    }

    /**
     * 구독 ID로 엔티티 조회 (내부 헬퍼)
     */
    private Subscription findOwnedSubscription(Integer memberId, Integer subscriptionId) {
        return subscriptionRepository.findByMemberMemberIdAndSubscriptionId(memberId, subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    private Member findMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateSubscriptionLimit(Member member) {
        Membership membership = member.getMembership();
        if (membership == Membership.PREMIUM) {
            return;
        }

        long currentCount = subscriptionRepository.countByMemberMemberId(member.getMemberId());
        if (currentCount >= membership.getSubscriptionLimit()) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED);
        }
    }

    private void validateDuplicateSubscription(
            Integer memberId,
            com.seein.domain.subscription.entity.StudyLanguage studyLanguage,
            com.seein.domain.subscription.entity.ExplanationLanguage explanationLanguage,
            com.seein.domain.subscription.entity.LearningStyle learningStyle,
            com.seein.domain.subscription.entity.DifficultyLevel difficultyLevel,
            LocalTime deliveryTime,
            Integer excludeSubscriptionId
    ) {
        boolean duplicated = subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .anyMatch(subscription -> !Objects.equals(subscription.getSubscriptionId(), excludeSubscriptionId)
                        && subscription.getStudyLanguage() == studyLanguage
                        && subscription.getExplanationLanguage() == explanationLanguage
                        && subscription.getLearningStyle() == learningStyle
                        && subscription.getDifficultyLevel() == difficultyLevel
                        && subscription.getDeliveryTime().equals(deliveryTime));

        if (duplicated) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    private String formatTime(LocalTime deliveryTime) {
        return deliveryTime.format(TIME_FORMATTER);
    }
}
