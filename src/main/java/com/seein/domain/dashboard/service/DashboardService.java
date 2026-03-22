package com.seein.domain.dashboard.service;

import com.seein.domain.dashboard.dto.DashboardResponse;
import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.keyword.service.KeywordService;
import com.seein.domain.news.dto.NewsResponse;
import com.seein.domain.news.repository.NewsCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대시보드 서비스
 * 메인 페이지에 필요한 인기 키워드와 최신 뉴스를 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final KeywordService keywordService;
    private final NewsCardRepository newsCardRepository;

    /** 인기 키워드 기본 조회 수 */
    private static final int DEFAULT_BEST_KEYWORD_LIMIT = 10;

    /**
     * 대시보드 데이터 조회
     * - 인기 키워드 TOP 10
     * - 전체 최신 뉴스 1건
     */
    public DashboardResponse getDashboard() {
        // 인기 키워드 TOP 10
        List<KeywordResponse> bestKeywords = keywordService.getTopKeywords(DEFAULT_BEST_KEYWORD_LIMIT);

        // 전체 최신 뉴스 1건
        NewsResponse recentNews = newsCardRepository
                .findAllByOrderByCreatedDateDesc(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(NewsResponse::from)
                .orElse(null);

        return new DashboardResponse(bestKeywords, recentNews);
    }
}
