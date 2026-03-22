package com.seein.domain.keyword.controller;

import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.keyword.service.KeywordService;
import com.seein.global.dto.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


/**
 * 키워드 API 컨트롤러
 * 키워드 검색/목록 조회, 인기 키워드 조회
 */
@Tag(name = "Keywords", description = "키워드 API")
@RestController
@RequestMapping("/api/v1/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    /**
     * 키워드 검색/목록 조회
     */
    @Operation(summary = "키워드 검색/목록", description = "전체 키워드를 검색하거나 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public GlobalResponseDto<Page<KeywordResponse>> searchKeywords(
            @Parameter(description = "키워드 검색어 (Optional)")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<KeywordResponse> response = keywordService.searchKeywords(search, pageable);
        return GlobalResponseDto.success(response);
    }

}
