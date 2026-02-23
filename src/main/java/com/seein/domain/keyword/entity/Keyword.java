package com.seein.domain.keyword.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 키워드 엔티티
 * 뉴스 검색 및 구독 키워드 관리
 */
@Entity
@Table(name = "keyword")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Integer keywordId;

    @Column(name = "keyword", unique = true, nullable = false)
    private String keyword;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 키워드 생성 (정적 팩토리 메서드)
     */
    public static Keyword create(String keyword) {
        Keyword entity = new Keyword();
        entity.keyword = keyword;
        return entity;
    }
}
