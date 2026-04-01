package com.seein.domain.home.controller;

import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.home.service.HomeFeedService;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private HomeFeedService homeFeedService;

    @Test
    @DisplayName("비로그인 사용자는 홈에 접근할 수 있다")
    void home_unauthenticated() {
        // given
        Model model = new ConcurrentModel();
        given(homeFeedService.getHomeFeed(StudyLanguage.ENGLISH, null))
                .willReturn(new HomeFeedResponse("ENGLISH", "영어", "ALL", "전체", null, java.util.List.of()));

        // when
        String viewName = homeController.home(StudyLanguage.ENGLISH, null, model, null);

        // then
        assertThat(viewName).isEqualTo("home");
        assertThat(model.getAttribute("isAuthenticated")).isEqualTo(false);
    }

    @Test
    @DisplayName("로그인 사용자는 홈에서 학습 피드 모델을 받는다")
    void home_authenticated() {
        // given
        Model model = new ConcurrentModel();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                AuthorityUtils.createAuthorityList("ROLE_NORMAL")
        );
        given(homeFeedService.getHomeFeed(StudyLanguage.JAPANESE, LearningStyle.BALANCED))
                .willReturn(new HomeFeedResponse("JAPANESE", "일본어", "BALANCED", "균형형", null, java.util.List.of()));

        // when
        String viewName = homeController.home(StudyLanguage.JAPANESE, LearningStyle.BALANCED, model, authentication);

        // then
        assertThat(viewName).isEqualTo("home");
        assertThat(model.getAttribute("isAuthenticated")).isEqualTo(true);
        assertThat(model.getAttribute("feed")).isInstanceOf(HomeFeedResponse.class);
    }
}
