package com.seein.global.exception;

import com.seein.global.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 ErrorResponse 형식으로 변환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request
    ) {
        log.error("BusinessException occurred: {}", ex.getMessage(), ex);
        
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 일반 Exception 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.of(
                500,
                "Internal Server Error",
                "서버 내부 오류가 발생했습니다.",
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity
                .status(500)
                .body(response);
    }
}
