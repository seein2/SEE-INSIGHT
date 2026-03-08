package com.seein.domain.dashboard.service;

import com.seein.domain.dashboard.dto.DashboardResponse;
import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.keyword.service.KeywordService;
import com.seein.domain.news.entity.NewsCard;
import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.news.repository.NewsCardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * DashboardService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private KeywordService keywordService;

    @Mock
    private NewsCardRepository newsCardRepository;

    @Test
    @DisplayName("대시보드 조회 - 인기 키워드와 최신 뉴스 모두 존재")
    void getDashboard_success() {
        // given
        List<KeywordResponse> keywords = List.of(
                new KeywordResponse(1, "삼성전자", 100L),
                new KeywordResponse(2, "AI", 80L)
        );
        given(keywordService.getTopKeywords(10)).willReturn(keywords);

        Keyword keyword = Keyword.create("삼성전자");
        NewsCard newsCard = NewsCard.create(keyword, "최신 뉴스 요약", "https://news.com", LocalDate.now());
        given(newsCardRepository.findAllByOrderByCreatedDateDesc(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(newsCard)));

        // when
        DashboardResponse result = dashboardService.getDashboard();

        // then
        assertThat(result.getBestKeywords()).hasSize(2);
        assertThat(result.getBestKeywords().get(0).getKeyword()).isEqualTo("삼성전자");
        assertThat(result.getRecentNews()).isNotNull();
        assertThat(result.getRecentNews().getSummaryContent()).isEqualTo("최신 뉴스 요약");
    }

    @Test
    @DisplayName("대시보드 조회 - 뉴스가 없을 때 recentNews는 null")
    void getDashboard_noNews() {
        // given
        given(keywordService.getTopKeywords(10)).willReturn(Collections.emptyList());
        given(newsCardRepository.findAllByOrderByCreatedDateDesc(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        // when
        DashboardResponse result = dashboardService.getDashboard();

        // then
        assertThat(result.getBestKeywords()).isEmpty();
        assertThat(result.getRecentNews()).isNull();
    }
}
