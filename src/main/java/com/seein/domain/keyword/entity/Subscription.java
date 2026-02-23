package com.seein.domain.keyword.entity;

import com.seein.domain.member.entity.Member;
import com.seein.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 구독 엔티티
 * 회원의 키워드 구독 정보 관리
 */
@Entity
@Table(
    name = "subscription",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "subscription_index_0",
            columnNames = {"member_id", "keyword_id"}
        )
    }
)
@Getter
@ToString(exclude = {"member", "keyword"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "notification_time")
    private String notificationTime;

    /**
     * 구독 생성 (정적 팩토리 메서드)
     */
    public static Subscription create(Member member, Keyword keyword, String notificationTime) {
        Subscription subscription = new Subscription();
        subscription.member = member;
        subscription.keyword = keyword;
        subscription.notificationTime = notificationTime;
        subscription.isActive = true;
        return subscription;
    }

    /**
     * 알림 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 알림 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 알림 시간 변경
     */
    public void updateNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }
}
