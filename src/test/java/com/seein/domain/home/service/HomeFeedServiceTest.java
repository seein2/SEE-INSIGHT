package com.seein.domain.home.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HomeFeedServiceTest {

    @InjectMocks
    private HomeFeedService homeFeedService;

    @Mock
    private LearningContentService learningContentService;

    @Test
    @DisplayName("홈 피드는 기본 언어를 영어로 선택하고 첫 카드를 추천 카드로 노출한다")
    void getHomeFeed_defaultLanguage() {
        // given
        LearningContentCardResponse card = new LearningContentCardResponse(
                1,
                "오늘의 균형 학습",
                "요약",
                "원문",
                "해설",
                "표현1",
                "표현2",
                "질문",
                "https://example.com",
                "ENGLISH",
                "영어",
                "KOREAN",
                "한국어",
                "BALANCED",
                "균형형",
                "BEGINNER",
                "초급",
                LocalDate.now()
        );
        given(learningContentService.getFeedCards(StudyLanguage.ENGLISH, null)).willReturn(List.of(card));

        // when
        HomeFeedResponse response = homeFeedService.getHomeFeed(null, null);

        // then
        assertThat(response.getSelectedStudyLanguage()).isEqualTo("ENGLISH");
        assertThat(response.getSelectedLearningStyle()).isEqualTo("ALL");
        assertThat(response.getFeaturedContent().getTitle()).isEqualTo("오늘의 균형 학습");
    }

    @Test
    @DisplayName("스타일 필터를 선택하면 동일 필터로 콘텐츠를 조회한다")
    void getHomeFeed_withStyleFilter() {
        // given
        given(learningContentService.getFeedCards(StudyLanguage.JAPANESE, LearningStyle.DAILY_CONVERSATION))
                .willReturn(List.of());

        // when
        HomeFeedResponse response = homeFeedService.getHomeFeed(StudyLanguage.JAPANESE, LearningStyle.DAILY_CONVERSATION);

        // then
        assertThat(response.getSelectedStudyLanguage()).isEqualTo("JAPANESE");
        assertThat(response.getSelectedLearningStyle()).isEqualTo("DAILY_CONVERSATION");
    }
}
