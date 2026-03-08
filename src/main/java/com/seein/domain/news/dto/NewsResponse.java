package com.seein.domain.news.dto;

import com.seein.domain.news.entity.NewsCard;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 뉴스 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class NewsResponse {

    private final Integer newsId;
    private final Integer keywordId;
    private final String keyword;
    private final String summaryContent;
    private final String sourceLink;
    private final LocalDate createdDate;

    /**
     * NewsCard 엔티티 → DTO 변환
     */
    public static NewsResponse from(NewsCard newsCard) {
        return new NewsResponse(
                newsCard.getNewsId(),
                newsCard.getKeyword().getKeywordId(),
                newsCard.getKeyword().getKeyword(),
                newsCard.getSummaryContent(),
                newsCard.getSourceLink(),
                newsCard.getCreatedDate()
        );
    }
}
