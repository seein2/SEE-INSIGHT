package com.seein.domain.content.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LearningContentTextSanitizerTest {

    private final LearningContentTextSanitizer sanitizer = new LearningContentTextSanitizer();

    @Test
    @DisplayName("HTML entity와 태그가 섞인 snippet을 깨끗한 원문 excerpt로 정제한다")
    void sanitizeSourceText_decodesEntitiesAndRemovesTags() {
        // given
        String rawSnippet = "Becca Well, &#x27;<strong>you&#x27;ve got me</strong>&#x27; comes from the phrase "
                + "&#x27;I&#x27;ve got your back&#x27;, which literally means to support someone. "
                + "Imagine coming down a ladder and your friend is behind you and helping to hold you up or making sure that ...";

        // when
        String sourceText = sanitizer.sanitizeSourceText(List.of(rawSnippet));

        // then
        assertThat(sourceText).contains("'you've got me'");
        assertThat(sourceText).doesNotContain("<strong>", "</strong>", "&#x27;", "...");
        assertThat(sourceText).doesNotEndWith("...");
    }

    @Test
    @DisplayName("너무 짧거나 깨진 snippet은 원문으로 사용하지 않는다")
    void sanitizeSourceText_rejectsUnusableSnippet() {
        // when
        String sourceText = sanitizer.sanitizeSourceText(List.of("short <strong>bad</strong>"));

        // then
        assertThat(sourceText).isNull();
    }
}
