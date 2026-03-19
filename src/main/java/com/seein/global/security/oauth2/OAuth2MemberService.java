package com.seein.global.security.oauth2;

import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.security.jwt.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 사용자 정보 로드 서비스
 * Google 로그인 후 사용자 정보를 DB에 저장하거나 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2MemberService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;

        // OAuth2 속성 추출
        if("naver".equals(provider)){
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else if("google".equals(provider)){
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        log.info("OAuth2 로그인 시도: provider={}, email={}", provider, email);

        // 회원 조회 또는 생성
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseGet(() -> {
                    log.info("신규 회원 등록: email={}", email);
                    Member newMember = Member.create(email, name, provider);
                    return memberRepository.save(newMember);
                });

        return new MemberPrincipal(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getMembership().name(),
                attributes
        );
    }
}
