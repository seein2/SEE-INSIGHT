package com.seein.domain.keyword.controller;

import com.seein.domain.keyword.dto.SubscriptionCreateRequest;
import com.seein.domain.keyword.dto.SubscriptionResponse;
import com.seein.domain.keyword.dto.SubscriptionUpdateRequest;
import com.seein.domain.keyword.service.SubscriptionService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 구독 API 컨트롤러
 * 키워드 구독 CRUD 기능 제공
 */
@Tag(name = "Subscriptions", description = "구독 API")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 내 구독 목록 조회
     */
    @Operation(summary = "내 구독 목록 조회", description = "현재 사용자가 구독 중인 키워드 목록을 조회합니다.")
    @GetMapping
    public GlobalResponseDto<Page<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SubscriptionResponse> response = subscriptionService.getSubscriptions(
                userDetails.getMemberId(), pageable);
        return GlobalResponseDto.success(response);
    }

    /**
     * 키워드 구독
     */
    @Operation(summary = "키워드 구독", description = "새로운 키워드를 구독합니다. 키워드가 없으면 자동 생성됩니다.")
    @PostMapping
    public GlobalResponseDto<SubscriptionResponse> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionCreateRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(
                userDetails.getMemberId(), request);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 상세 조회
     */
    @Operation(summary = "구독 상세 조회", description = "특정 구독의 상세 정보를 조회합니다.")
    @GetMapping("/{subscriptionId}")
    public GlobalResponseDto<SubscriptionResponse> getSubscription(
            @PathVariable Integer subscriptionId) {
        SubscriptionResponse response = subscriptionService.getSubscription(subscriptionId);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 설정 변경
     */
    @Operation(summary = "구독 설정 변경", description = "구독의 알림 시간 또는 활성화 상태를 변경합니다.")
    @PatchMapping("/{subscriptionId}")
    public GlobalResponseDto<SubscriptionResponse> updateSubscription(
            @PathVariable Integer subscriptionId,
            @RequestBody SubscriptionUpdateRequest request) {
        SubscriptionResponse response = subscriptionService.updateSubscription(subscriptionId, request);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 취소
     */
    @Operation(summary = "구독 취소", description = "해당 키워드 구독을 삭제합니다.")
    @DeleteMapping("/{subscriptionId}")
    public GlobalResponseDto<String> unsubscribe(
            @PathVariable Integer subscriptionId) {
        subscriptionService.unsubscribe(subscriptionId);
        return GlobalResponseDto.success("구독이 취소되었습니다.");
    }
}
