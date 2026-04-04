package com.seein.global.security.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 프로퍼티
 * application.yaml의 jwt 설정 바인딩
 */
@Getter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessExpiration;
    private long refreshExpiration;

    /**
     * 시크릿 키 설정
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 액세스 토큰 만료 시간 설정
     */
    public void setAccessExpiration(long accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    /**
     * 리프레시 토큰 만료 시간 설정
     */
    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
}
