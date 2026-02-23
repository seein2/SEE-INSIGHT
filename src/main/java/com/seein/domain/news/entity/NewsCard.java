package com.seein.domain.news.entity;

import com.seein.domain.keyword.entity.Keyword;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 뉴스 카드 엔티티
 * AI가 생성한 키워드별 뉴스 요약 정보
 */
@Entity
@Table(
    name = "news_card",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "news_card_index_1",
            columnNames = {"created_date", "keyword_id"}
        )
    }
)
@Getter
@ToString(exclude = "keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Integer newsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(name = "summary_content", columnDefinition = "text")
    private String summaryContent;

    @Column(name = "source_link", length = 2048)
    private String sourceLink;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    /**
     * 뉴스 카드 생성 (정적 팩토리 메서드)
     */
    public static NewsCard create(Keyword keyword, String summaryContent, String sourceLink, LocalDate createdDate) {
        NewsCard newsCard = new NewsCard();
        newsCard.keyword = keyword;
        newsCard.summaryContent = summaryContent;
        newsCard.sourceLink = sourceLink;
        newsCard.createdDate = createdDate;
        return newsCard;
    }

    /**
     * 요약 내용 업데이트
     */
    public void updateSummary(String summaryContent) {
        this.summaryContent = summaryContent;
    }
}
