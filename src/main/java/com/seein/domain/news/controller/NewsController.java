package com.seein.domain.news.controller;

import com.seein.domain.news.dto.NewsResponse;
import com.seein.domain.news.service.NewsService;
import com.seein.global.dto.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 뉴스 API 컨트롤러
 * 뉴스 목록 조회, 상세 조회 기능 제공
 */
@Tag(name = "News", description = "뉴스 API")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 뉴스 목록 조회 (키워드, 날짜 필터)
     */
    @Operation(summary = "뉴스 목록 조회", description = "키워드 또는 날짜 기준으로 뉴스 요약을 조회합니다.")
    @GetMapping
    public GlobalResponseDto<Page<NewsResponse>> getNewsList(
            @RequestParam(required = false) Integer keywordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsResponse> response = newsService.getNewsList(keywordId, date, pageable);
        return GlobalResponseDto.success(response);
    }

    /**
     * 뉴스 상세 조회
     */
    @Operation(summary = "뉴스 상세 조회", description = "특정 뉴스의 상세 정보를 조회합니다.")
    @GetMapping("/{newsId}")
    public GlobalResponseDto<NewsResponse> getNews(@PathVariable Integer newsId) {
        NewsResponse response = newsService.getNews(newsId);
        return GlobalResponseDto.success(response);
    }
}
