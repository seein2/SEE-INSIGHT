package com.seein.domain.keyword.service;

import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.keyword.repository.KeywordRepository;
import com.seein.domain.keyword.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 키워드 서비스
 * 키워드 검색/목록 조회, 인기 키워드 TOP N 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 키워드 검색/목록 조회 (페이징)
     * search 파라미터가 없으면 전체 목록, 있으면 검색
     */
    public Page<KeywordResponse> searchKeywords(String search, Pageable pageable) {
        Page<Keyword> keywords;

        if (search == null || search.isBlank()) {
            keywords = keywordRepository.findAll(pageable);
        } else {
            keywords = keywordRepository.findByKeywordContaining(search, pageable);
        }

        return keywords.map(keyword -> new KeywordResponse(
                keyword.getKeywordId(),
                keyword.getKeyword(),
                subscriptionRepository.countByKeywordId(keyword.getKeywordId())
        ));
    }

    /**
     * 인기 키워드 TOP N 조회 (구독자 수 기준 내림차순)
     */
    public List<KeywordResponse> getTopKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = subscriptionRepository.findTopKeywords(pageable);

        return results.stream()
                .map(row -> new KeywordResponse(
                        (Integer) row[0],
                        (String) row[1],
                        (Long) row[2]
                ))
                .collect(Collectors.toList());
    }
}
