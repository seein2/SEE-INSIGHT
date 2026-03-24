package com.seein.domain.news.scheduler;

import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.domain.news.entity.NewsCard;
import com.seein.domain.news.entity.SendLog;
import com.seein.domain.news.repository.SendLogRepository;
import com.seein.domain.news.service.EmailService;
import com.seein.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 뉴스 스케줄러
 * 매일 오전 8시에 활성 구독 대상으로 뉴스를 자동 생성하고 이메일을 발송한다.
 * 실패 전략: Log and Continue (한 건 실패해도 나머지 계속 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NewsService newsService;
    private final EmailService emailService;
    private final SendLogRepository sendLogRepository;

    /**
     * 뉴스 자동 생성 및 이메일 발송 스케줄러
     * 매일 오전 8시(KST)에 실행
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendDailyNews() {
        log.info("===== 뉴스 자동 발송 스케줄러 시작 =====");
        LocalDateTime issueDate = LocalDateTime.now();

        // 1. 활성 구독 목록 조회
        List<Subscription> activeSubscriptions = subscriptionRepository.findByIsActiveTrue();
        log.info("활성 구독 수: {}", activeSubscriptions.size());

        if (activeSubscriptions.isEmpty()) {
            log.info("활성 구독이 없어 스케줄러를 종료합니다.");
            return;
        }

        // 2. 키워드별 뉴스 생성 (동일 키워드는 한 번만 생성하여 캐싱)
        Map<Integer, NewsCard> newsCache = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Subscription subscription : activeSubscriptions) {
            try {
                Keyword keyword = subscription.getKeyword();
                Integer keywordId = keyword.getKeywordId();

                // 키워드별 뉴스 캐싱 (동일 키워드에 대해 중복 API 호출 방지)
                NewsCard newsCard = newsCache.get(keywordId);
                if (newsCard == null) {
                    newsCard = newsService.generateNewsForKeyword(keyword);
                    if (newsCard != null) {
                        newsCache.put(keywordId, newsCard);
                    }
                }

                // 뉴스 생성 실패 시 해당 구독 건은 스킵
                if (newsCard == null) {
                    log.warn("뉴스 생성 실패로 이메일 발송 스킵 - subscriptionId: {}, keyword: {}",
                            subscription.getSubscriptionId(), keyword.getKeyword());
                    SendLog failLog = SendLog.createFailure(subscription, issueDate, "뉴스 생성 실패");
                    sendLogRepository.save(failLog);
                    failCount++;
                    continue;
                }

                // 3. 이메일 발송
                emailService.sendNewsEmail(subscription, newsCard);

                // 4. 발송 성공 로그 기록
                SendLog successLog = SendLog.createSuccess(subscription, issueDate);
                sendLogRepository.save(successLog);
                successCount++;

            } catch (Exception e) {
                // Log and Continue: 한 건 실패해도 나머지 계속 처리
                log.error("이메일 발송 실패 - subscriptionId: {}, error: {}",
                        subscription.getSubscriptionId(), e.getMessage());
                SendLog failLog = SendLog.createFailure(subscription, issueDate, e.getMessage());
                sendLogRepository.save(failLog);
                failCount++;
            }
        }

        log.info("===== 뉴스 자동 발송 스케줄러 완료 - 성공: {}, 실패: {}, 전체: {} =====",
                successCount, failCount, activeSubscriptions.size());
    }
}
