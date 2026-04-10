package com.seein.domain.member.controller;

import com.seein.domain.member.dto.MemberResponse;
import com.seein.domain.member.dto.MyPageResponse;
import com.seein.domain.member.service.MemberService;
import com.seein.global.security.jwt.MemberPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberPageControllerTest {

    @InjectMocks
    private MemberPageController memberPageController;

    @Mock
    private MemberService memberService;

    @Test
    @DisplayName("내 정보 페이지를 조회하면 마이 페이지 정보가 모델에 담긴다")
    void me_success() {
        MemberPrincipal principal = new MemberPrincipal(1, "test@example.com", "테스터", "NORMAL", Map.of());
        Model model = new ConcurrentModel();
        MyPageResponse response = new MyPageResponse(
                1,
                "test@example.com",
                "테스터",
                "NORMAL",
                "일반",
                "google",
                LocalDateTime.of(2026, 4, 10, 12, 0),
                0,
                "1개",
                false,
                List.of()
        );
        given(memberService.getMyPage(1)).willReturn(response);

        String viewName = memberPageController.me(principal, model);

        assertThat(viewName).isEqualTo("my/me");
        assertThat(model.getAttribute("myPage")).isEqualTo(response);
    }

    @Test
    @DisplayName("회원정보 수정 페이지를 조회하면 회원 정보가 모델에 담긴다")
    void edit_success() {
        MemberPrincipal principal = new MemberPrincipal(1, "test@example.com", "테스터", "NORMAL", Map.of());
        Model model = new ConcurrentModel();
        MemberResponse response = new MemberResponse(
                1,
                "test@example.com",
                "테스터",
                "NORMAL",
                "google",
                LocalDateTime.of(2026, 4, 10, 12, 0)
        );
        given(memberService.getMember(1)).willReturn(response);

        String viewName = memberPageController.edit(principal, model);

        assertThat(viewName).isEqualTo("my/edit");
        assertThat(model.getAttribute("member")).isEqualTo(response);
        assertThat(model.getAttribute("updateApiUrl")).isEqualTo("/api/v1/members/me/nickname");
        assertThat(model.getAttribute("myPageUrl")).isEqualTo("/me");
    }
}
