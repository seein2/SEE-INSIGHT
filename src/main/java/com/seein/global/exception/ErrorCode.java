package com.seein.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 에러 코드 정의
 * HTTP 상태 코드와 에러 메시지를 관리
 */
@Getter
public enum ErrorCode {
    // 인증/인가 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    
    // 회원 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),

    // 학습 구독 관련 에러
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "학습 구독 정보를 찾을 수 없습니다."),
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 학습 설정이 이미 존재합니다."),
    SUBSCRIPTION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "현재 멤버십으로 생성 가능한 구독 수를 초과했습니다."),

    // 학습 콘텐츠 관련 에러
    LEARNING_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학습 콘텐츠를 찾을 수 없습니다."),
    LEARNING_CONTENT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "학습 콘텐츠 생성에 실패했습니다."),

    // 외부 API 에러
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출에 실패했습니다."),

    // 일반 에러
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
