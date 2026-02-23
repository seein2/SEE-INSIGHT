package com.seein.global.dto;

import lombok.Getter;
import lombok.ToString;

/**
 * API 응답 공통 래퍼 클래스
 * 모든 API 응답은 이 형식으로 통일
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@ToString
public class GlobalResponseDto<T> {
    private final int status;
    private final String message;
    private final T data;

    private GlobalResponseDto(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> GlobalResponseDto<T> success(T data) {
        return new GlobalResponseDto<>(200, "Success", data);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> GlobalResponseDto<T> success(String message) {
        return new GlobalResponseDto<>(200, message, null);
    }

    /**
     * 성공 응답 생성 (커스텀 상태 코드 및 메시지)
     */
    public static <T> GlobalResponseDto<T> success(int status, String message, T data) {
        return new GlobalResponseDto<>(status, message, data);
    }
}
