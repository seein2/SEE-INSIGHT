package com.seein.domain.delivery.entity;

import com.seein.domain.content.entity.LearningContent;
import com.seein.domain.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 이메일 발송 로그 엔티티
 */
@Entity
@Table(name = "delivery_log")
@Getter
@ToString(exclude = {"subscription", "learningContent"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_log_id")
    private Integer deliveryLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private LearningContent learningContent;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "fail_reason", columnDefinition = "text")
    private String failReason;

    public static DeliveryLog createSuccess(Subscription subscription, LearningContent learningContent, LocalDate issueDate) {
        DeliveryLog log = new DeliveryLog();
        log.subscription = subscription;
        log.learningContent = learningContent;
        log.sentAt = LocalDateTime.now();
        log.status = DeliveryStatus.SUCCESS;
        log.issueDate = issueDate;
        return log;
    }

    public static DeliveryLog createFailure(Subscription subscription, LearningContent learningContent, LocalDate issueDate, String failReason) {
        DeliveryLog log = new DeliveryLog();
        log.subscription = subscription;
        log.learningContent = learningContent;
        log.sentAt = LocalDateTime.now();
        log.status = DeliveryStatus.FAIL;
        log.issueDate = issueDate;
        log.failReason = failReason;
        return log;
    }
}
