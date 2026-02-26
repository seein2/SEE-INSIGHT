package com.seein.global.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 사용자 정보 래퍼
 * OAuth2 로그인 성공 시 사용자 정보 저장
 */
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Integer memberId;
    private final String email;
    private final String nickname;
    private final String membershipRole;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + membershipRole));
    }

    @Override
    public String getName() {
        return email;
    }
}
