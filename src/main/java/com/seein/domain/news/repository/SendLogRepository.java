package com.seein.domain.news.repository;

import com.seein.domain.news.entity.SendLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 발송 로그 Repository
 */
public interface SendLogRepository extends JpaRepository<SendLog, Integer> {
}
