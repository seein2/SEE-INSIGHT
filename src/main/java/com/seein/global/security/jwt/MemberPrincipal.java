package com.seein.global.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 서비스 전역 인증 사용자 정보
 * OAuth2 로그인 직후와 JWT 인증 이후 모두 동일한 Principal을 사용한다.
 */
@Getter
@RequiredArgsConstructor
public class MemberPrincipal implements UserDetails, OAuth2User {

    private final Integer memberId;
    private final String email;
    private final String nickname;
    private final String membershipRole;
    private final Map<String, Object> attributes;

    /**
     * OAuth2 속성 반환
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 권한 목록 반환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + membershipRole));
    }

    /**
     * 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return null; // OAuth2 로그인이므로 비밀번호 없음
    }

    /**
     * 사용자명 반환
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 이름 반환
     */
    @Override
    public String getName() {
        return email;
    }

    /**
     * 계정 만료 여부 반환
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 반환
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명 만료 여부 반환
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 활성 여부 반환
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
