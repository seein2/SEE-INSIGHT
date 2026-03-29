package com.seein.domain.news.repository;

import com.seein.domain.news.entity.SendLog;
import com.seein.domain.news.entity.SendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 발송 로그 Repository
 */
public interface SendLogRepository extends JpaRepository<SendLog, Integer> {

    /**
     * 같은 구독에 대해 오늘 이미 발송 성공한 기록이 있는지 확인
     */
    boolean existsBySubscriptionSubscriptionIdAndStatusAndIssueDateBetween(
            Integer subscriptionId,
            SendStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}
