package com.seein.domain.auth.service;

import com.seein.domain.auth.dto.OAuth2MemberInfo;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import com.seein.global.security.jwt.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 인증 대상 회원 조회 및 생성 서비스
 * Spring Security 어댑터에서 사용하는 실제 인증 회원 유스케이스를 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthMemberService {

    private final MemberRepository memberRepository;

    /**
     * 이메일로 현재 활성 회원을 조회하여 Principal 생성
     */
    public MemberPrincipal loadPrincipalByEmail(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return toPrincipal(member, Collections.emptyMap());
    }

    /**
     * OAuth2 공급자 사용자 정보를 기준으로 회원을 조회하거나 생성
     */
    @Transactional
    public MemberPrincipal loadOrCreateOAuth2Member(String provider, Map<String, Object> attributes) {
        OAuth2MemberInfo memberInfo = extractOAuth2MemberInfo(provider, attributes);

        log.info("OAuth2 로그인 시도: provider={}, email={}", memberInfo.getProvider(), memberInfo.getEmail());

        Member member = memberRepository.findByEmailAndDeletedAtIsNull(memberInfo.getEmail())
                .orElseGet(() -> {
                    log.info("신규 회원 등록: email={}", memberInfo.getEmail());
                    Member newMember = Member.create(memberInfo.getEmail(), memberInfo.getNickname(), memberInfo.getProvider());
                    return memberRepository.save(newMember);
                });

        return toPrincipal(member, memberInfo.getAttributes());
    }

    private OAuth2MemberInfo extractOAuth2MemberInfo(String provider, Map<String, Object> attributes) {
        if ("naver".equals(provider)) {
            Object response = attributes.get("response");
            if (!(response instanceof Map<?, ?> responseAttributes)) {
                throw new IllegalArgumentException("Invalid naver attributes");
            }

            String email = getRequiredAttribute(responseAttributes, "email");
            String nickname = getRequiredAttribute(responseAttributes, "name");
            return OAuth2MemberInfo.of(email, nickname, provider, attributes);
        }

        if ("google".equals(provider)) {
            String email = getRequiredAttribute(attributes, "email");
            String nickname = getRequiredAttribute(attributes, "name");
            return OAuth2MemberInfo.of(email, nickname, provider, attributes);
        }

        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private String getRequiredAttribute(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        throw new IllegalArgumentException("Missing OAuth2 attribute: " + key);
    }

    private MemberPrincipal toPrincipal(Member member, Map<String, Object> attributes) {
        return new MemberPrincipal(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getMembership().name(),
                attributes
        );
    }
}
