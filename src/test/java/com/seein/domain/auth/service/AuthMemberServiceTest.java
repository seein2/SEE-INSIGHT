package com.seein.domain.auth.service;

import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.security.jwt.MemberPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthMemberServiceTest {

    @InjectMocks
    private AuthMemberService authMemberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 활성 회원을 조회해 Principal을 생성한다")
    void loadPrincipalByEmail_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        given(memberRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                .willReturn(Optional.of(member));

        // when
        MemberPrincipal principal = authMemberService.loadPrincipalByEmail("test@example.com");

        // then
        assertThat(principal.getEmail()).isEqualTo("test@example.com");
        assertThat(principal.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("구글 OAuth2 속성으로 회원을 조회하거나 생성한다")
    void loadOrCreateOAuth2Member_google_success() {
        // given
        Map<String, Object> attributes = Map.of(
                "email", "google@example.com",
                "name", "구글유저"
        );
        Member member = Member.create("google@example.com", "구글유저", "google");
        given(memberRepository.findByEmailAndDeletedAtIsNull("google@example.com"))
                .willReturn(Optional.of(member));

        // when
        MemberPrincipal principal = authMemberService.loadOrCreateOAuth2Member("google", attributes);

        // then
        assertThat(principal.getEmail()).isEqualTo("google@example.com");
        assertThat(principal.getNickname()).isEqualTo("구글유저");
        assertThat(principal.getAttributes()).isEqualTo(attributes);
    }
}
