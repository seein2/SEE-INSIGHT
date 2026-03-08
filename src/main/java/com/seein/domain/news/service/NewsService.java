package com.seein.domain.news.service;

import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.keyword.repository.KeywordRepository;
import com.seein.domain.news.dto.NewsResponse;
import com.seein.domain.news.entity.NewsCard;
import com.seein.domain.news.repository.NewsCardRepository;
import com.seein.global.config.PerplexityClient;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 뉴스 서비스
 * 뉴스 목록/상세 조회 및 Perplexity API를 통한 뉴스 요약 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsCardRepository newsCardRepository;
    private final KeywordRepository keywordRepository;
    private final PerplexityClient perplexityClient;

    /**
     * 뉴스 목록 조회 (키워드, 날짜 필터 지원)
     */
    public Page<NewsResponse> getNewsList(Integer keywordId, LocalDate date, Pageable pageable) {
        Page<NewsCard> newsCards;

        if (keywordId != null && date != null) {
            // 키워드 + 날짜 필터
            newsCards = newsCardRepository.findByKeywordKeywordIdAndCreatedDateOrderByCreatedDateDesc(
                    keywordId, date, pageable);
        } else if (keywordId != null) {
            // 키워드 필터만
            newsCards = newsCardRepository.findByKeywordKeywordIdOrderByCreatedDateDesc(keywordId, pageable);
        } else if (date != null) {
            // 날짜 필터만
            newsCards = newsCardRepository.findByCreatedDateOrderByCreatedDateDesc(date, pageable);
        } else {
            // 전체 목록
            newsCards = newsCardRepository.findAllByOrderByCreatedDateDesc(pageable);
        }

        return newsCards.map(NewsResponse::from);
    }

    /**
     * 뉴스 상세 조회
     */
    public NewsResponse getNews(Integer newsId) {
        NewsCard newsCard = newsCardRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));
        return NewsResponse.from(newsCard);
    }

    /**
     * 키워드 기반 뉴스 요약 생성 (Perplexity API 호출)
     * 동일 키워드 + 날짜 조합의 뉴스가 이미 존재하면 생성하지 않음
     */
    @Transactional
    public NewsResponse generateNews(Integer keywordId) {
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.KEYWORD_NOT_FOUND));

        LocalDate today = LocalDate.now();

        // 오늘 이미 생성된 뉴스가 있으면 기존 뉴스 반환
        if (newsCardRepository.existsByKeywordKeywordIdAndCreatedDate(keywordId, today)) {
            NewsCard existing = newsCardRepository
                    .findTopByKeywordKeywordIdOrderByCreatedDateDesc(keywordId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));
            return NewsResponse.from(existing);
        }

        // Perplexity API 호출하여 뉴스 요약 생성
        String rawResponse = perplexityClient.generateNewsWithSource(keyword.getKeyword());
        String summaryContent = perplexityClient.extractContentFromResponse(rawResponse);
        String sourceLink = perplexityClient.extractFirstCitation(rawResponse);

        // NewsCard 엔티티 생성 및 저장
        NewsCard newsCard = NewsCard.create(keyword, summaryContent, sourceLink, today);
        newsCardRepository.save(newsCard);

        log.info("뉴스 생성 완료 - keyword: {}, newsId: {}", keyword.getKeyword(), newsCard.getNewsId());
        return NewsResponse.from(newsCard);
    }

    /**
     * 키워드 문자열 기반 뉴스 요약 생성 (스케줄러에서 사용)
     * 키워드가 DB에 없으면 예외 발생
     */
    @Transactional
    public NewsCard generateNewsForKeyword(Keyword keyword) {
        LocalDate today = LocalDate.now();

        // 오늘 이미 생성된 뉴스가 있으면 기존 뉴스 반환
        if (newsCardRepository.existsByKeywordKeywordIdAndCreatedDate(keyword.getKeywordId(), today)) {
            return newsCardRepository
                    .findTopByKeywordKeywordIdOrderByCreatedDateDesc(keyword.getKeywordId())
                    .orElse(null);
        }

        try {
            String rawResponse = perplexityClient.generateNewsWithSource(keyword.getKeyword());
            String summaryContent = perplexityClient.extractContentFromResponse(rawResponse);
            String sourceLink = perplexityClient.extractFirstCitation(rawResponse);

            NewsCard newsCard = NewsCard.create(keyword, summaryContent, sourceLink, today);
            newsCardRepository.save(newsCard);

            log.info("뉴스 자동 생성 완료 - keyword: {}", keyword.getKeyword());
            return newsCard;
        } catch (Exception e) {
            log.error("뉴스 자동 생성 실패 - keyword: {}, error: {}", keyword.getKeyword(), e.getMessage());
            return null;
        }
    }
}
