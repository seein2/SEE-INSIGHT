package com.seein.domain.member.service;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.MyPageResponse;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 서비스
 * 회원 정보 조회, 닉네임 변경, 회원 탈퇴 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 회원 ID로 회원 정보 조회
     */
    public MemberResponse getMember(Integer memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public String updateNickname(Integer memberId, String nickname) {
        Member member = findMemberById(memberId);
        member.updateNickname(nickname);
        return member.getNickname();
    }

    /**
     * 마이 페이지 정보 조회
     */
    public MyPageResponse getMyPage(Integer memberId) {
        Member member = findMemberById(memberId);
        List<SubscriptionResponse> subscriptions = subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();

        return new MyPageResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getMembership().name(),
                member.getMembership().getLabel(),
                member.getProvider(),
                member.getCreatedAt(),
                subscriptions.size(),
                member.getMembership().getSubscriptionLimit() == Integer.MAX_VALUE ? "무제한" : member.getMembership().getSubscriptionLimit() + "개",
                member.getMembership() == com.seein.domain.member.entity.Membership.PREMIUM,
                subscriptions
        );
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    @Transactional
    public void withdraw(Integer memberId) {
        Member member = findMemberById(memberId);
        List<Subscription> subscriptions = subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId);
        subscriptions.forEach(Subscription::deactivate);
        member.softDelete();
    }

    /**
     * 회원 ID로 엔티티 조회 (내부 헬퍼)
     */
    private Member findMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
