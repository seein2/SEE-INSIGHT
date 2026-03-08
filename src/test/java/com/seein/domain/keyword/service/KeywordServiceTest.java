package com.seein.domain.keyword.service;

import com.seein.domain.keyword.dto.KeywordResponse;
import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.keyword.repository.KeywordRepository;
import com.seein.domain.keyword.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * KeywordService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @InjectMocks
    private KeywordService keywordService;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("키워드 전체 목록 조회 (검색어 없음)")
    void searchKeywords_noSearch() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        Page<Keyword> page = new PageImpl<>(List.of(keyword));
        Pageable pageable = PageRequest.of(0, 20);

        given(keywordRepository.findAll(pageable)).willReturn(page);
        given(subscriptionRepository.countByKeywordId(any())).willReturn(5L);

        // when
        Page<KeywordResponse> result = keywordService.searchKeywords(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getKeyword()).isEqualTo("삼성전자");
        assertThat(result.getContent().get(0).getSubscriberCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("키워드 검색 조회")
    void searchKeywords_withSearch() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        Page<Keyword> page = new PageImpl<>(List.of(keyword));
        Pageable pageable = PageRequest.of(0, 20);

        given(keywordRepository.findByKeywordContaining(eq("삼성"), eq(pageable))).willReturn(page);
        given(subscriptionRepository.countByKeywordId(any())).willReturn(3L);

        // when
        Page<KeywordResponse> result = keywordService.searchKeywords("삼성", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSubscriberCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("인기 키워드 TOP N 조회")
    void getTopKeywords_success() {
        // given
        List<Object[]> mockResults = List.of(
                new Object[]{1, "삼성전자", 100L},
                new Object[]{2, "AI", 80L}
        );
        given(subscriptionRepository.findTopKeywords(any(Pageable.class))).willReturn(mockResults);

        // when
        List<KeywordResponse> result = keywordService.getTopKeywords(10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getKeyword()).isEqualTo("삼성전자");
        assertThat(result.get(0).getSubscriberCount()).isEqualTo(100L);
        assertThat(result.get(1).getKeyword()).isEqualTo("AI");
    }
}
