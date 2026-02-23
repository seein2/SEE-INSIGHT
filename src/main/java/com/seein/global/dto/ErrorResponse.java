package com.seein.global.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 * 모든 예외는 이 형식으로 응답
 */
@Getter
@ToString
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    private ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path);
    }
}
