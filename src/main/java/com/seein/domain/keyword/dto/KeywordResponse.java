package com.seein.domain.keyword.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 키워드 응답 DTO
 * 구독자 수 포함
 */
@Getter
@RequiredArgsConstructor
public class KeywordResponse {

    private final Integer keywordId;
    private final String keyword;
    private final Long subscriberCount;

    public KeywordResponse(Integer keywordId, String keyword) {
        this.keywordId = keywordId;
        this.keyword = keyword;
        this.subscriberCount = 0L;
    }
}
