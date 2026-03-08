package com.seein.domain.news.service;

import com.seein.domain.keyword.entity.Keyword;
import com.seein.domain.keyword.repository.KeywordRepository;
import com.seein.domain.news.dto.NewsResponse;
import com.seein.domain.news.entity.NewsCard;
import com.seein.domain.news.repository.NewsCardRepository;
import com.seein.global.config.PerplexityClient;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * NewsService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    private NewsService newsService;

    @Mock
    private NewsCardRepository newsCardRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private PerplexityClient perplexityClient;

    @Test
    @DisplayName("뉴스 목록 조회 - 전체")
    void getNewsList_all() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        NewsCard newsCard = NewsCard.create(keyword, "요약 내용", "https://source.com", LocalDate.now());
        Page<NewsCard> page = new PageImpl<>(List.of(newsCard));
        Pageable pageable = PageRequest.of(0, 10);

        given(newsCardRepository.findAllByOrderByCreatedDateDesc(pageable)).willReturn(page);

        // when
        Page<NewsResponse> result = newsService.getNewsList(null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSummaryContent()).isEqualTo("요약 내용");
    }

    @Test
    @DisplayName("뉴스 목록 조회 - 키워드 필터")
    void getNewsList_byKeyword() {
        // given
        Keyword keyword = Keyword.create("AI");
        NewsCard newsCard = NewsCard.create(keyword, "AI 뉴스 요약", "https://ai.com", LocalDate.now());
        Page<NewsCard> page = new PageImpl<>(List.of(newsCard));
        Pageable pageable = PageRequest.of(0, 10);

        given(newsCardRepository.findByKeywordKeywordIdOrderByCreatedDateDesc(eq(1), eq(pageable)))
                .willReturn(page);

        // when
        Page<NewsResponse> result = newsService.getNewsList(1, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("뉴스 상세 조회 성공")
    void getNews_success() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        NewsCard newsCard = NewsCard.create(keyword, "상세 요약", "https://detail.com", LocalDate.now());
        given(newsCardRepository.findById(1)).willReturn(Optional.of(newsCard));

        // when
        NewsResponse result = newsService.getNews(1);

        // then
        assertThat(result.getSummaryContent()).isEqualTo("상세 요약");
        assertThat(result.getSourceLink()).isEqualTo("https://detail.com");
    }

    @Test
    @DisplayName("존재하지 않는 뉴스 조회 시 예외 발생")
    void getNews_notFound() {
        // given
        given(newsCardRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> newsService.getNews(999))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NEWS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("뉴스 생성 - 오늘 이미 생성된 뉴스가 있으면 기존 뉴스 반환")
    void generateNews_alreadyExists() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        NewsCard existingCard = NewsCard.create(keyword, "기존 요약", "https://existing.com", LocalDate.now());

        given(keywordRepository.findById(1)).willReturn(Optional.of(keyword));
        given(newsCardRepository.existsByKeywordKeywordIdAndCreatedDate(any(), any())).willReturn(true);
        given(newsCardRepository.findTopByKeywordKeywordIdOrderByCreatedDateDesc(any()))
                .willReturn(Optional.of(existingCard));

        // when
        NewsResponse result = newsService.generateNews(1);

        // then
        assertThat(result.getSummaryContent()).isEqualTo("기존 요약");
        verify(perplexityClient, never()).generateNewsWithSource(any());
    }

    @Test
    @DisplayName("스케줄러용 뉴스 생성 - API 호출 실패 시 null 반환")
    void generateNewsForKeyword_failure() {
        // given
        Keyword keyword = Keyword.create("삼성전자");
        given(newsCardRepository.existsByKeywordKeywordIdAndCreatedDate(any(), any())).willReturn(false);
        given(perplexityClient.generateNewsWithSource("삼성전자"))
                .willThrow(new RuntimeException("API 오류"));

        // when
        NewsCard result = newsService.generateNewsForKeyword(keyword);

        // then
        assertThat(result).isNull();
    }
}
