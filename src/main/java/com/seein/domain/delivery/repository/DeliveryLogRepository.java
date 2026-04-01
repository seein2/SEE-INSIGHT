package com.seein.domain.delivery.repository;

import com.seein.domain.delivery.entity.DeliveryLog;
import com.seein.domain.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

/**
 * 발송 로그 Repository
 */
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Integer> {

    boolean existsBySubscriptionSubscriptionIdAndStatusAndIssueDate(
            Integer subscriptionId,
            DeliveryStatus status,
            LocalDate issueDate
    );
}
