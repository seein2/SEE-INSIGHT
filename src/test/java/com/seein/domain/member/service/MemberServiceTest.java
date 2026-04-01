package com.seein.domain.member.service;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.MyPageResponse;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * MemberService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMember_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        given(memberRepository.findById(1)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMember(1);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getProvider()).isEqualTo("google");
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외 발생")
    void getMember_notFound() {
        // given
        given(memberRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMember(999))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void updateNickname_success() {
        // given
        Member member = Member.create("test@example.com", "기존닉네임", "google");
        given(memberRepository.findById(1)).willReturn(Optional.of(member));

        // when
        String result = memberService.updateNickname(1, "새닉네임");

        // then
        assertThat(result).isEqualTo("새닉네임");
        assertThat(member.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("마이 페이지 조회 성공")
    void getMyPage_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        Subscription subscription = Subscription.create(
                member,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalTime.of(8, 0)
        );
        given(memberRepository.findById(1)).willReturn(Optional.of(member));
        given(subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(1)).willReturn(List.of(subscription));

        // when
        MyPageResponse response = memberService.getMyPage(1);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getCurrentSubscriptionCount()).isEqualTo(1);
        assertThat(response.getSubscriptionLimitLabel()).isEqualTo("1개");
    }

    @Test
    @DisplayName("회원 탈퇴 성공 (소프트 삭제)")
    void withdraw_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        Subscription subscription = Subscription.create(
                member,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalTime.of(8, 0)
        );
        given(memberRepository.findById(1)).willReturn(Optional.of(member));
        given(subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(1)).willReturn(List.of(subscription));

        // when
        memberService.withdraw(1);

        // then
        assertThat(member.isDeleted()).isTrue();
        assertThat(subscription.getIsActive()).isFalse();
    }
}
