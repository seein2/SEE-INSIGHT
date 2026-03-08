package com.seein.domain.member.service;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    @DisplayName("회원 탈퇴 성공 (소프트 삭제)")
    void withdraw_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        given(memberRepository.findById(1)).willReturn(Optional.of(member));

        // when
        memberService.withdraw(1);

        // then
        assertThat(member.isDeleted()).isTrue();
    }
}
