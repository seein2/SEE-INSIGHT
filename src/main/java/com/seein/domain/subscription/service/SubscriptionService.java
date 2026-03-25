package com.seein.domain.subscription.service;

import com.seein.domain.subscription.dto.SubscriptionCreateRequest;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.dto.SubscriptionUpdateRequest;
import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.keyword.repository.KeywordRepository;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 서비스
 * 키워드 구독 생성, 목록 조회, 상세 조회, 설정 변경, 구독 취소
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final KeywordRepository keywordRepository;
    private final MemberRepository memberRepository;

    /**
     * 내 구독 목록 조회 (페이징)
     */
    public Page<SubscriptionResponse> getSubscriptions(Integer memberId, Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository.findByMemberMemberId(memberId, pageable);
        return subscriptions.map(SubscriptionResponse::from);
    }

    /**
     * 키워드 구독 생성
     * 키워드가 존재하지 않으면 자동 생성
     */
    @Transactional
    public SubscriptionResponse subscribe(Integer memberId, SubscriptionCreateRequest request) {
        Member member = findMemberById(memberId);

        // 키워드 조회 또는 자동 생성
        Keyword keyword = keywordRepository.findByKeyword(request.getKeyword())
                .orElseGet(() -> keywordRepository.save(Keyword.create(request.getKeyword())));

        // 중복 구독 검증
        if (subscriptionRepository.existsByMemberMemberIdAndKeywordKeywordId(
                memberId, keyword.getKeywordId())) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }

        Subscription subscription = Subscription.create(member, keyword);
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
     * 구독 설정 변경 (활성화 상태, 알림 시간)
     */
    @Transactional
    public SubscriptionResponse updateSubscription(Integer memberId, Integer subscriptionId, SubscriptionUpdateRequest request) {
        Subscription subscription = findOwnedSubscription(memberId, subscriptionId);

        if (request.getIsActive() != null) {
            if (request.getIsActive()) {
                subscription.activate();
            } else {
                subscription.deactivate();
            }
        }

        if (request.getNotificationTime() != null) {
            subscription.updateNotificationTime(request.getNotificationTime());
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

    /**
     * 회원 ID로 엔티티 조회 (내부 헬퍼)
     */
    private Member findMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
