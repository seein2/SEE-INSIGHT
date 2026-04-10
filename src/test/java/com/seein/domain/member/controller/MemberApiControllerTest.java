package com.seein.domain.member.controller;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.MemberUpdateRequest;
import com.seein.domain.member.service.MemberService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.MemberPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberApiControllerTest {

    @InjectMocks
    private MemberApiController memberApiController;

    @Mock
    private MemberService memberService;

    @Test
    @DisplayName("내 회원 정보 수정 API는 수정 후 최신 회원 정보를 응답한다")
    void updateMyMember_success() {
        // given
        MemberPrincipal principal = new MemberPrincipal(1, "old@example.com", "기존닉네임", "NORMAL", Map.of());
        MemberUpdateRequest request = new MemberUpdateRequest();
        ReflectionTestUtils.setField(request, "nickname", "새닉네임");
        MemberResponse response = new MemberResponse(
                1,
                "old@example.com",
                "새닉네임",
                "NORMAL",
                "google",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );
        given(memberService.getMember(1)).willReturn(response);

        // when
        GlobalResponseDto<MemberResponse> result = memberApiController.updateMyMember(principal, request);

        // then
        verify(memberService).updateMemberInfo(1, "새닉네임");
        assertThat(result.getData().getEmail()).isEqualTo("old@example.com");
        assertThat(result.getData().getNickname()).isEqualTo("새닉네임");
    }
}
