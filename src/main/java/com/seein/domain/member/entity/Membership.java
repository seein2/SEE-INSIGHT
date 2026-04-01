package com.seein.domain.member.entity;

/**
 * 회원 등급 Enum
 * NORMAL: 일반 회원
 * PREMIUM: 프리미엄 회원
 */
public enum Membership {
    NORMAL,
    PREMIUM;

    public int getSubscriptionLimit() {
        return this == PREMIUM ? Integer.MAX_VALUE : 1;
    }

    public String getLabel() {
        return this == PREMIUM ? "Premium" : "Normal";
    }
}
