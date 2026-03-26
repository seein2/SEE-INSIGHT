package com.seein.global.security.jwt;

import com.seein.domain.auth.service.AuthMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 구현체
 * JWT 토큰에서 추출한 이메일로 사용자 로드
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthMemberService authMemberService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authMemberService.loadPrincipalByEmail(email);
    }
}
