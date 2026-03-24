package com.seein.domain.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import com.seein.domain.dashboard.service.DashboardService;
import com.seein.domain.dashboard.dto.DashboardResponse;
import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.global.security.handler.JwtAccessDeniedHandler;
import com.seein.global.security.handler.JwtAuthenticationEntryPoint;
import com.seein.global.security.handler.OAuth2FailureHandler;
import com.seein.global.security.handler.OAuth2SuccessHandler;
import com.seein.global.security.jwt.JwtAuthenticationFilter;
import com.seein.global.security.jwt.JwtTokenProvider;
import com.seein.global.security.oauth2.OAuth2MemberService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private OAuth2MemberService oAuth2MemberService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private HomeController homeController;

    @Test
    @DisplayName("비로그인 상태에서 홈 페이지 접근")
    void home_unauthenticated() throws Exception {
        Model model = mock(Model.class);
        String result = homeController.home(model, null);
        
        assertEquals("home", result);
    }

    @Test
    @DisplayName("로그인 상태에서 홈 페이지 접근")
    void home_authenticated() throws Exception {
        Model model = mock(Model.class);
        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
        
        String result = homeController.home(model, auth);
        
        assertEquals("home", result);
    }

    @Test
    @DisplayName("홈 페이지에 bestKeywords 모델 속성 전달")
    void home_bestKeywords() throws Exception {
        List<KeywordResponse> keywords = new ArrayList<>();
        keywords.add(new KeywordResponse(1, "Spring"));
        keywords.add(new KeywordResponse(2, "Java"));
        
        DashboardResponse dashResponse = new DashboardResponse(keywords, null);
        when(dashboardService.getDashboard()).thenReturn(dashResponse);

        Model model = mock(Model.class);
        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
        
        String result = homeController.home(model, auth);
        
        assertEquals("home", result);
    }
}
