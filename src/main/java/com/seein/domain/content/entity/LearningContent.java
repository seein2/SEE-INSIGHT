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

    @Column(name = "source_text", columnDefinition = "text", nullable = false)
    private String sourceText;

    @Column(name = "expression_one", length = 255)
    private String expressionOne;

    @Column(name = "expression_two", length = 255)
    private String expressionTwo;

    @Column(name = "source_link", length = 2048)
    private String sourceLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_source_type", length = 30)
    private ContentSourceType contentSourceType;

    @Column(name = "source_name", length = 120)
    private String sourceName;

    @Column(name = "source_host", length = 255)
    private String sourceHost;

    @Column(name = "source_page_age", length = 120)
    private String sourcePageAge;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "raw_snippets", columnDefinition = "text")
    private String rawSnippets;

    @Column(name = "generation_version", length = 30)
    private String generationVersion;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    /**
     * 학습 콘텐츠 생성
     */
    public static LearningContent create(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            String title,
            String sourceText,
            String expressionOne,
            String expressionTwo,
            String sourceLink,
            LocalDate publishedDate
    ) {
        return createWithMetadata(
                studyLanguage,
                explanationLanguage,
                learningStyle,
                difficultyLevel,
                title,
                sourceText,
                expressionOne,
                expressionTwo,
                sourceLink,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                publishedDate
        );
    }

    /**
     * 학습 콘텐츠 생성 (원천 메타데이터 포함)
     */
    public static LearningContent createWithMetadata(
            StudyLanguage studyLanguage,
            ExplanationLanguage explanationLanguage,
            LearningStyle learningStyle,
            DifficultyLevel difficultyLevel,
            String title,
            String sourceText,
            String expressionOne,
            String expressionTwo,
            String sourceLink,
            ContentSourceType contentSourceType,
            String sourceName,
            String sourceHost,
            String sourcePageAge,
            Integer qualityScore,
            String rawSnippets,
            String generationVersion,
            LocalDate publishedDate
    ) {
        LearningContent content = new LearningContent();
        content.studyLanguage = studyLanguage;
        content.explanationLanguage = explanationLanguage;
        content.learningStyle = learningStyle;
        content.difficultyLevel = difficultyLevel;
        content.title = title;
        content.sourceText = sourceText;
        content.expressionOne = expressionOne;
        content.expressionTwo = expressionTwo;
        content.sourceLink = sourceLink;
        content.contentSourceType = contentSourceType;
        content.sourceName = sourceName;
        content.sourceHost = sourceHost;
        content.sourcePageAge = sourcePageAge;
        content.qualityScore = qualityScore;
        content.rawSnippets = rawSnippets;
        content.generationVersion = generationVersion;
        content.publishedDate = publishedDate;
        return content;
    }
}
