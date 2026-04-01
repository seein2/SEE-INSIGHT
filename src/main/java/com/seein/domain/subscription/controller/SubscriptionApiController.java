package com.seein.domain.subscription.controller;

import com.seein.domain.subscription.dto.SubscriptionCreateRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewResponse;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.dto.SubscriptionUpdateRequest;
import com.seein.domain.subscription.service.SubscriptionService;
import com.seein.global.dto.GlobalResponseDto;
import com.seein.global.security.jwt.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 구독 API 컨트롤러
 * 키워드 구독 CRUD 기능 제공
 */
@Tag(name = "Subscriptions", description = "키워드 구독 API")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionApiController {

    private final SubscriptionService subscriptionService;

    /**
     * 내 구독 목록 조회
     */
    @Operation(summary = "내 학습 구독 목록 조회", description = "현재 로그인한 사용자의 학습 이메일 구독 목록을 조회합니다.")
    @GetMapping
    public GlobalResponseDto<java.util.List<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return GlobalResponseDto.success(subscriptionService.getSubscriptions(principal.getMemberId()));
    }

    /**
     * 구독 미리보기 생성
     */
    @Operation(summary = "구독 미리보기", description = "학습 설정 기반 이메일 미리보기를 생성합니다.")
    @PostMapping("/preview")
    public GlobalResponseDto<SubscriptionPreviewResponse> preview(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody SubscriptionPreviewRequest request
    ) {
        return GlobalResponseDto.success(subscriptionService.preview(principal.getMemberId(), request));
    }

    /**
     * 학습 구독 생성
     */
    @Operation(summary = "학습 구독 생성", description = "새로운 학습 이메일 구독을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "409", description = "동일한 구독이 이미 존재하거나 구독 한도를 초과함")
    })
    @PostMapping
    public GlobalResponseDto<SubscriptionResponse> subscribe(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        SubscriptionResponse response = subscriptionService.subscribe(principal.getMemberId(), request);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 상세 조회
     */
    @Operation(summary = "구독 상세 조회", description = "특정 구독의 상세 정보를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"), @ApiResponse(responseCode = "404", description = "구독 정보를 찾을 수 없음")})
    @GetMapping("/{subscriptionId}")
    public GlobalResponseDto<SubscriptionResponse> getSubscription(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Integer subscriptionId) {
        SubscriptionResponse response = subscriptionService.getSubscription(principal.getMemberId(), subscriptionId);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 설정 변경
     */
    @Operation(summary = "구독 설정 변경", description = "구독의 학습 설정 또는 활성화 상태를 변경합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "변경 성공"), @ApiResponse(responseCode = "404", description = "구독 정보를 찾을 수 없음")})
    @PatchMapping("/{subscriptionId}")
    public GlobalResponseDto<SubscriptionResponse> updateSubscription(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Integer subscriptionId,
            @RequestBody SubscriptionUpdateRequest request
    ) {
        SubscriptionResponse response = subscriptionService.updateSubscription(principal.getMemberId(), subscriptionId, request);
        return GlobalResponseDto.success(response);
    }

    /**
     * 구독 취소
     */
    @Operation(summary = "구독 취소", description = "해당 키워드 구독을 삭제합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "취소 성공"), @ApiResponse(responseCode = "404", description = "구독 정보를 찾을 수 없음")})
    @DeleteMapping("/{subscriptionId}")
    public GlobalResponseDto<String> unsubscribe(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Integer subscriptionId) {
        subscriptionService.unsubscribe(principal.getMemberId(), subscriptionId);
        return GlobalResponseDto.success("학습 구독이 삭제되었습니다.");
    }
}
