package com.seein.domain.dashboard.controller;

import com.seein.domain.dashboard.dto.DashboardResponse;
import com.seein.domain.dashboard.service.DashboardService;
import com.seein.global.dto.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API 컨트롤러
 * 메인 페이지 데이터 (인기 키워드 + 최신 뉴스) 제공
 */
@Tag(name = "Dashboard", description = "대시보드 API")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 메인 페이지 데이터 조회
     * 인기 키워드 목록과 최신 뉴스 1건
     */
    @Operation(summary = "메인 페이지 데이터", description = "인기 키워드 목록과 최신 뉴스 1건을 조회합니다.")
    @GetMapping
    public GlobalResponseDto<DashboardResponse> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard();
        return GlobalResponseDto.success(response);
    }
}
