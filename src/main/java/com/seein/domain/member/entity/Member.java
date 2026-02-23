package com.seein.domain.member.entity;

import com.seein.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * OAuth2 로그인 사용자 정보 관리
 */
@Entity
@Table(name = "member")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership")
    private Membership membership;

    @Column(name = "provider")
    private String provider;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 회원 생성 (정적 팩토리 메서드)
     */
    public static Member create(String email, String nickname, String provider) {
        Member member = new Member();
        member.email = email;
        member.nickname = nickname;
        member.provider = provider;
        member.membership = Membership.NORMAL;
        return member;
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 프리미엄으로 등급 변경
     */
    public void upgradeToPremium() {
        this.membership = Membership.PREMIUM;
    }

    /**
     * 일반 등급으로 다운그레이드
     */
    public void downgradeToNormal() {
        this.membership = Membership.NORMAL;
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
