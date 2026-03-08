package com.seein.domain.keyword.repository;

import com.seein.domain.keyword.entity.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 키워드 Repository
 */
public interface KeywordRepository extends JpaRepository<Keyword, Integer> {

    /**
     * 키워드명으로 조회
     */
    Optional<Keyword> findByKeyword(String keyword);

    /**
     * 키워드명 포함 검색 (페이징)
     */
    Page<Keyword> findByKeywordContaining(String keyword, Pageable pageable);

    /**
     * 키워드 존재 여부 확인
     */
    boolean existsByKeyword(String keyword);
}
