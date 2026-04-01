package com.seein.domain.subscription.service;

import com.seein.domain.content.dto.LearningContentCardResponse;
import com.seein.domain.content.service.LearningContentService;
import com.seein.domain.member.entity.Member;
import com.seein.domain.member.repository.MemberRepository;
import com.seein.domain.subscription.dto.SubscriptionCreateRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewRequest;
import com.seein.domain.subscription.dto.SubscriptionPreviewResponse;
import com.seein.domain.subscription.dto.SubscriptionResponse;
import com.seein.domain.subscription.dto.SubscriptionUpdateRequest;
import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.Subscription;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.domain.subscription.repository.SubscriptionRepository;
import com.seein.global.exception.BusinessException;
import com.seein.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LearningContentService learningContentService;

    @Test
    @DisplayName("Normal 회원은 구독 1개를 초과해 생성할 수 없다")
    void subscribe_limitExceeded() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        ReflectionTestUtils.setField(member, "memberId", 1);
        SubscriptionCreateRequest request = createSubscriptionRequest(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalTime.of(8, 0)
        );
        given(memberRepository.findById(1)).willReturn(Optional.of(member));
        given(subscriptionRepository.countByMemberMemberId(1)).willReturn(1L);

        // when & then
        assertThatThrownBy(() -> subscriptionService.subscribe(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("학습 구독 미리보기는 선택한 설정을 그대로 응답한다")
    void preview_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        SubscriptionPreviewRequest request = createPreviewRequest(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER,
                LocalTime.of(8, 0)
        );
        LearningContentCardResponse preview = new LearningContentCardResponse(
                1, "오늘의 회화", "요약", "원문", "해설", "표현1", "표현2", "질문",
                "https://example.com", "ENGLISH", "영어", "KOREAN", "한국어",
                "DAILY_CONVERSATION", "일상 회화", "BEGINNER", "초급", LocalDate.now()
        );
        given(memberRepository.findById(1)).willReturn(Optional.of(member));
        given(learningContentService.getPreviewContent(
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.DAILY_CONVERSATION,
                DifficultyLevel.BEGINNER
        )).willReturn(preview);

        // when
        SubscriptionPreviewResponse response = subscriptionService.preview(1, request);

        // then
        assertThat(response.getStudyLanguage()).isEqualTo("ENGLISH");
        assertThat(response.getLearningStyle()).isEqualTo("DAILY_CONVERSATION");
        assertThat(response.getPreviewContent().getTitle()).isEqualTo("오늘의 회화");
    }

    @Test
    @DisplayName("구독 수정 시 새 설정으로 업데이트된다")
    void updateSubscription_success() {
        // given
        Member member = Member.create("test@example.com", "테스터", "google");
        Subscription subscription = Subscription.create(
                member,
                StudyLanguage.ENGLISH,
                ExplanationLanguage.KOREAN,
                LearningStyle.BALANCED,
                DifficultyLevel.BEGINNER,
                LocalTime.of(8, 0)
        );
        ReflectionTestUtils.setField(subscription, "subscriptionId", 1);
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
        ReflectionTestUtils.setField(request, "learningStyle", LearningStyle.PRACTICAL_READING);
        ReflectionTestUtils.setField(request, "difficultyLevel", DifficultyLevel.INTERMEDIATE);
        ReflectionTestUtils.setField(request, "deliveryTime", LocalTime.of(19, 0));

        given(subscriptionRepository.findByMemberMemberIdAndSubscriptionId(1, 1)).willReturn(Optional.of(subscription));
        given(subscriptionRepository.findByMemberMemberIdOrderByCreatedAtDesc(1)).willReturn(List.of(subscription));

        // when
        SubscriptionResponse response = subscriptionService.updateSubscription(1, 1, request);

        // then
        assertThat(response.getLearningStyle()).isEqualTo("PRACTICAL_READING");
        assertThat(response.getDifficultyLevel()).isEqualTo("INTERMEDIATE");
        assertThat(response.getDeliveryTime()).isEqualTo("19:00");
    }

    private SubscriptionCreateRequest createSubscriptionRequest(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalTime deliveryTime
    ) {
        SubscriptionCreateRequest request = new SubscriptionCreateRequest();
        ReflectionTestUtils.setField(request, "studyLanguage", studyLanguage);
        ReflectionTestUtils.setField(request, "explanationLanguage", explanationLanguage);
        ReflectionTestUtils.setField(request, "learningStyle", learningStyle);
        ReflectionTestUtils.setField(request, "difficultyLevel", difficultyLevel);
        ReflectionTestUtils.setField(request, "deliveryTime", deliveryTime);
        return request;
    }

    private SubscriptionPreviewRequest createPreviewRequest(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            LocalTime deliveryTime
    ) {
        SubscriptionPreviewRequest request = new SubscriptionPreviewRequest();
        ReflectionTestUtils.setField(request, "studyLanguage", studyLanguage);
        ReflectionTestUtils.setField(request, "explanationLanguage", explanationLanguage);
        ReflectionTestUtils.setField(request, "learningStyle", learningStyle);
        ReflectionTestUtils.setField(request, "difficultyLevel", difficultyLevel);
        ReflectionTestUtils.setField(request, "deliveryTime", deliveryTime);
        return request;
    }
}
