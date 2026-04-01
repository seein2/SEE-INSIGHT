package com.seein.global.exception;

import com.seein.global.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 ErrorResponse 형식으로 변환
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * API 요청 여부 판단
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return uri.startsWith("/api/") || (accept != null && accept.contains("application/json"));
    }

    /**
     * 404 Not Found 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("Resource not found: {}", request.getRequestURI());

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    404,
                    "Not Found",
                    "요청한 리소스를 찾을 수 없습니다.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request, HttpServletResponse response, WebRequest webRequest) throws IOException {
        log.error("BusinessException occurred: {}", ex.getMessage(), ex);

        ErrorCode errorCode = ex.getErrorCode();

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    errorCode.getStatus().value(),
                    errorCode.getStatus().getReasonPhrase(),
                    ex.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
        }

        response.sendError(errorCode.getStatus().value());
        return null;
    }

    /**
     * Validation 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.INVALID_INPUT.getMessage();

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * JSON 파싱/enum 바인딩 에러 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCode.INVALID_INPUT.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Query parameter enum/type 변환 에러 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCode.INVALID_INPUT.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 일반 Exception 처리 (500 에러)
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    500,
                    "Internal Server Error",
                    "서버 내부 오류가 발생했습니다.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return null;
    }
}
