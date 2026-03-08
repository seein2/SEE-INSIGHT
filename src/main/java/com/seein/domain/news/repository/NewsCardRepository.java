package com.seein.domain.news.repository;

import com.seein.domain.news.entity.NewsCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 뉴스 카드 Repository
 */
public interface NewsCardRepository extends JpaRepository<NewsCard, Integer> {

    /**
     * 키워드 ID로 뉴스 목록 조회 (페이징, 최신순)
     */
    Page<NewsCard> findByKeywordKeywordIdOrderByCreatedDateDesc(Integer keywordId, Pageable pageable);

    /**
     * 날짜로 뉴스 목록 조회 (페이징)
     */
    Page<NewsCard> findByCreatedDateOrderByCreatedDateDesc(LocalDate date, Pageable pageable);

    /**
     * 키워드 ID + 날짜로 뉴스 목록 조회 (페이징)
     */
    Page<NewsCard> findByKeywordKeywordIdAndCreatedDateOrderByCreatedDateDesc(
            Integer keywordId, LocalDate date, Pageable pageable);

    /**
     * 전체 뉴스 목록 조회 (최신순, 페이징)
     */
    Page<NewsCard> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /**
     * 키워드 + 날짜 조합으로 뉴스 존재 여부 확인 (중복 생성 방지)
     */
    boolean existsByKeywordKeywordIdAndCreatedDate(Integer keywordId, LocalDate date);

    /**
     * 키워드의 최신 뉴스 1건 조회
     */
    Optional<NewsCard> findTopByKeywordKeywordIdOrderByCreatedDateDesc(Integer keywordId);
}
