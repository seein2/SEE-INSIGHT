package com.seein.domain.news.entity;

import com.seein.domain.keyword.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 발송 로그 엔티티
 * 이메일 발송 기록 및 성공/실패 추적
 */
@Entity
@Table(name = "send_log")
@Getter
@ToString(exclude = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SendStatus status;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "fail_reason", columnDefinition = "text")
    private String failReason;

    /**
     * 발송 성공 로그 생성 (정적 팩토리 메서드)
     */
    public static SendLog createSuccess(Subscription subscription, LocalDateTime issueDate) {
        SendLog log = new SendLog();
        log.subscription = subscription;
        log.sentAt = LocalDateTime.now();
        log.status = SendStatus.SUCCESS;
        log.issueDate = issueDate;
        return log;
    }

    /**
     * 발송 실패 로그 생성 (정적 팩토리 메서드)
     */
    public static SendLog createFailure(Subscription subscription, LocalDateTime issueDate, String failReason) {
        SendLog log = new SendLog();
        log.subscription = subscription;
        log.sentAt = LocalDateTime.now();
        log.status = SendStatus.FAIL;
        log.issueDate = issueDate;
        log.failReason = failReason;
        return log;
    }
}
