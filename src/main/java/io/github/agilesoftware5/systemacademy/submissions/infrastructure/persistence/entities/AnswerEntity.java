package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "answers")
public class AnswerEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private SubmissionEntity submission;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "selected_option_id")
    private UUID selectedOptionId;

    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_status", nullable = false, length = 30)
    private GradingStatus gradingStatus;

    @Column(name = "ai_general_feedback", columnDefinition = "text")
    private String aiGeneralFeedback;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "answer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("position ASC")
    private List<CriterionResultEntity> criterionResults = new ArrayList<>();

    protected AnswerEntity() {
    }

    public AnswerEntity(
            UUID id,
            SubmissionEntity submission,
            UUID questionId,
            UUID selectedOptionId,
            String answerText,
            Boolean isCorrect,
            GradingStatus gradingStatus,
            String aiGeneralFeedback,
            String aiModel,
            String errorMessage,
            UUID reviewedBy,
            OffsetDateTime reviewedAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.submission = submission;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
        this.gradingStatus = gradingStatus;
        this.aiGeneralFeedback = aiGeneralFeedback;
        this.aiModel = aiModel;
        this.errorMessage = errorMessage;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public SubmissionEntity getSubmission() {
        return submission;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public UUID getSelectedOptionId() {
        return selectedOptionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public GradingStatus getGradingStatus() {
        return gradingStatus;
    }

    public String getAiGeneralFeedback() {
        return aiGeneralFeedback;
    }

    public String getAiModel() {
        return aiModel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CriterionResultEntity> getCriterionResults() {
        return criterionResults;
    }
}
