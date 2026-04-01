package com.seein.domain.content.entity;

import com.seein.domain.subscription.entity.DifficultyLevel;
import com.seein.domain.subscription.entity.ExplanationLanguage;
import com.seein.domain.subscription.entity.LearningStyle;
import com.seein.domain.subscription.entity.StudyLanguage;
import com.seein.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 학습용 콘텐츠 엔티티
 */
@Entity
@Table(
        name = "learning_content",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "learning_content_unique_daily_settings",
                        columnNames = {
                                "study_language",
                                "explanation_language",
                                "learning_style",
                                "difficulty_level",
                                "published_date"
                        }
                )
        }
)
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Integer contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_language", nullable = false, length = 30)
    private StudyLanguage studyLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "explanation_language", nullable = false, length = 30)
    private ExplanationLanguage explanationLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style", nullable = false, length = 30)
    private LearningStyle learningStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 30)
    private DifficultyLevel difficultyLevel;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "summary", columnDefinition = "text", nullable = false)
    private String summary;

    @Column(name = "source_text", columnDefinition = "text", nullable = false)
    private String sourceText;

    @Column(name = "explanation_text", columnDefinition = "text", nullable = false)
    private String explanationText;

    @Column(name = "expression_one", length = 255)
    private String expressionOne;

    @Column(name = "expression_two", length = 255)
    private String expressionTwo;

    @Column(name = "quiz_text", columnDefinition = "text")
    private String quizText;

    @Column(name = "source_link", length = 2048)
    private String sourceLink;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    public static LearningContent create(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            String title,
            String summary,
            String sourceText,
            String explanationText,
            String expressionOne,
            String expressionTwo,
            String quizText,
            String sourceLink,
            LocalDate publishedDate
    ) {
        LearningContent content = new LearningContent();
        content.studyLanguage = studyLanguage;
        content.explanationLanguage = explanationLanguage;
        content.learningStyle = learningStyle;
        content.difficultyLevel = difficultyLevel;
        content.title = title;
        content.summary = summary;
        content.sourceText = sourceText;
        content.explanationText = explanationText;
        content.expressionOne = expressionOne;
        content.expressionTwo = expressionTwo;
        content.quizText = quizText;
        content.sourceLink = sourceLink;
        content.publishedDate = publishedDate;
        return content;
    }
}
