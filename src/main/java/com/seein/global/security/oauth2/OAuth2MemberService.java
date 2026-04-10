package com.seein.global.security.oauth2;

import com.seein.domain.auth.service.AuthMemberService;
import com.seein.global.security.jwt.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 사용자 정보 로드 서비스
 * Google 로그인 후 사용자 정보를 DB에 저장하거나 조회
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2MemberService extends DefaultOAuth2UserService {

    private final AuthMemberService authMemberService;

    /**
     * OAuth2 사용자 정보 로드
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        try {
            return authMemberService.loadOrCreateOAuth2Member(provider, oAuth2User.getAttributes());
        } catch (IllegalArgumentException e) {
            log.warn("OAuth2 사용자 정보 처리 실패: provider={}", provider, e);
            throw OAuth2LoginError.AUTHENTICATION_FAILED.toAuthenticationException(e);
        }
    }
}
