package com.seein.domain.dashboard.dto;

import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.news.dto.NewsResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 대시보드 응답 DTO
 * 인기 키워드 목록과 최신 뉴스 1건을 포함
 */
@Getter
@RequiredArgsConstructor
public class DashboardResponse {

    /** 인기 키워드 목록 (구독자 수 기준 상위 N개) */
    private final List<KeywordResponse> bestKeywords;

    /** 최신 뉴스 1건 (전체 기준) */
    private final NewsResponse recentNews;
}
