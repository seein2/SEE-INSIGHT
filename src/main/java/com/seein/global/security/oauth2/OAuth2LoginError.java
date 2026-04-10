package com.seein.global.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Arrays;
import java.util.Optional;

/**
 * OAuth2 로그인 실패 시 외부에 노출 가능한 에러 코드와 메시지
 * 내부 예외 메시지 대신 허용된 코드만 리다이렉트 파라미터로 전달한다.
 */
@Getter
@RequiredArgsConstructor
public enum OAuth2LoginError {
    ACCESS_DENIED("oauth2_access_denied", "소셜 로그인 동의가 취소되었거나 거부되었습니다. 다시 시도해 주세요."),
    AUTHENTICATION_FAILED("oauth2_authentication_failed", "소셜 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final String code;
    private final String message;

    /**
     * 리다이렉트 쿼리 파라미터를 기반으로 사용자 노출용 에러를 조회
     */
    public static Optional<OAuth2LoginError> fromCode(String code) {
        return Arrays.stream(values())
                .filter(error -> error.code.equals(code))
                .findFirst();
    }

    /**
     * Spring Security 인증 예외를 외부 노출 가능한 에러 코드로 변환
     */
    public static OAuth2LoginError fromException(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();
            if ("access_denied".equals(errorCode) || ACCESS_DENIED.code.equals(errorCode)) {
                return ACCESS_DENIED;
            }

            return fromCode(errorCode).orElse(AUTHENTICATION_FAILED);
        }

        return AUTHENTICATION_FAILED;
    }

    /**
     * 내부 예외를 OAuth2 인증 예외로 감싸되 외부에는 허용된 코드만 노출
     */
    public OAuth2AuthenticationException toAuthenticationException(Throwable cause) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(code),
                message,
                cause
        );
    }
}
