package com.seein.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 기본 클래스
 * 모든 커스텀 예외는 이 클래스를 상속
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
