package com.seein.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seein.global.dto.ErrorResponse;
import com.seein.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인가 실패 핸들러
 * 인증된 사용자가 권한이 없는 리소스에 접근 시 403 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Forbidden error: {}", accessDeniedException.getMessage());

        // Content Negotiation: 브라우저 vs API 요청 구분
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            // 브라우저 요청: HTML 에러 페이지로 forward
            request.getRequestDispatcher("/error/403.html").forward(request, response);
        } else {
            // API 요청: JSON 응답
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
