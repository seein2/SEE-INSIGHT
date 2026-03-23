package com.seein.domain.keyword.repository;

import com.seein.domain.keyword.entity.Subscription;
import com.seein.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 구독 Repository
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    /**
     * 회원의 구독 목록 조회 (페이징)
     */
    Page<Subscription> findByMemberMemberId(Integer memberId, Pageable pageable);

    /**
     * 회원 + 키워드로 구독 조회 (중복 방지)
     */
    Optional<Subscription> findByMemberMemberIdAndKeywordKeywordId(Integer memberId, Integer keywordId);

    /**
     * 회원 + 키워드 구독 존재 여부
     */
    boolean existsByMemberMemberIdAndKeywordKeywordId(Integer memberId, Integer keywordId);

    /**
     * 키워드별 구독자 수 조회
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.keyword.keywordId = :keywordId")
    Long countByKeywordId(@Param("keywordId") Integer keywordId);

    /**
     * 인기 키워드 TOP N (구독자 수 기준 내림차순)
     */
    @Query("SELECT s.keyword.keywordId, s.keyword.keyword, COUNT(s) as cnt " +
           "FROM Subscription s " +
           "GROUP BY s.keyword.keywordId, s.keyword.keyword " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopKeywords(Pageable pageable);

    /**
     * 활성 구독 목록 조회 (스케줄러용)
     */
    List<Subscription> findByIsActiveTrue();
}
