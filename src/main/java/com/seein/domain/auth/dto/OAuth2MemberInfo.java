package com.seein.domain.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * OAuth2 공급자에서 추출한 회원 정보
 */
@Getter
@RequiredArgsConstructor
public class OAuth2MemberInfo {

    private final String email;
    private final String nickname;
    private final String provider;
    private final Map<String, Object> attributes;

    public static OAuth2MemberInfo of(String email, String nickname, String provider, Map<String, Object> attributes) {
        return new OAuth2MemberInfo(email, nickname, provider, attributes);
    }
}
