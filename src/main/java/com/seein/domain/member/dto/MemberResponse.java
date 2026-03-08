package com.seein.domain.member.dto;

import com.seein.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 정보 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class MemberResponse {

    private final Integer memberId;
    private final String email;
    private final String nickname;
    private final String membership;
    private final String provider;
    private final LocalDateTime createdAt;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getMembership().name(),
                member.getProvider(),
                member.getCreatedAt()
        );
    }
}
