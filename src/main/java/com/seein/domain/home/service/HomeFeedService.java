package com.seein.domain.home.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.home.dto.HomeFeedResponse;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 홈 피드 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeFeedService {

    private final LearningContentService learningContentService;

    public HomeFeedResponse getHomeFeed(StudyLanguage studyLanguage, LearningStyle learningStyle) {
        StudyLanguage targetLanguage = studyLanguage != null ? studyLanguage : StudyLanguage.ENGLISH;
        List<LearningContentCardResponse> feedCards = learningContentService.getFeedCards(targetLanguage, learningStyle);
        LearningContentCardResponse featuredContent = feedCards.isEmpty() ? null : feedCards.get(0);

        return new HomeFeedResponse(
                targetLanguage.name(),
                targetLanguage.getLabel(),
                learningStyle != null ? learningStyle.name() : "ALL",
                learningStyle != null ? learningStyle.getLabel() : "전체",
                featuredContent,
                feedCards
        );
    }
}
