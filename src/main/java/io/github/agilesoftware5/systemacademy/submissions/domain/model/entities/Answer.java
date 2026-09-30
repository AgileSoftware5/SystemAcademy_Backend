package io.github.agilesoftware5.systemacademy.submissions.domain.model.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Answer {

    private final UUID id;
    private final UUID submissionId;
    private final UUID questionId;
    private String answerText;

    private GradingStatus gradingStatus;
    private String aiGeneralFeedback;
    private String aiModel;
    private String errorMessage;

    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime updatedAt;

    private final List<CriterionResult> criterionResults;

    private Answer(
            UUID id,
            UUID submissionId,
            UUID questionId,
            String answerText,
            GradingStatus gradingStatus,
            String aiGeneralFeedback,
            String aiModel,
            String errorMessage,
            UUID reviewedBy,
            OffsetDateTime reviewedAt,
            OffsetDateTime updatedAt,
            List<CriterionResult> criterionResults
    ) {
        this.id = id;
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.gradingStatus = gradingStatus;
        this.aiGeneralFeedback = aiGeneralFeedback;
        this.aiModel = aiModel;
        this.errorMessage = errorMessage;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.updatedAt = updatedAt;
        this.criterionResults = criterionResults;
    }

    public static Answer create(
            UUID submissionId,
            UUID questionId,
            String answerText
    ) {
        validateId(
                submissionId,
                "el id de la entrega no puede ser nulo"
        );

        validateId(
                questionId,
                "el id de la pregunta no puede ser nulo"
        );

        validateAnswerText(answerText);

        OffsetDateTime now = OffsetDateTime.now();

        return new Answer(
                UUID.randomUUID(),
                submissionId,
                questionId,
                answerText,
                GradingStatus.PENDING,
                null,
                null,
                null,
                null,
                null,
                now,
                new ArrayList<>()
        );
    }

    public static Answer fromPersistence(
            UUID id,
            UUID submissionId,
            UUID questionId,
            String answerText,
            GradingStatus gradingStatus,
            String aiGeneralFeedback,
            String aiModel,
            String errorMessage,
            UUID reviewedBy,
            OffsetDateTime reviewedAt,
            OffsetDateTime updatedAt,
            List<CriterionResult> criterionResults
    ) {
        validateId(
                id,
                "el id de la respuesta no puede ser nulo"
        );

        validateId(
                submissionId,
                "el id de la entrega no puede ser nulo"
        );

        validateId(
                questionId,
                "el id de la pregunta no puede ser nulo"
        );

        validateAnswerText(answerText);

        if (gradingStatus == null) {
            throw new IllegalArgumentException(
                    "el estado de evaluación no puede ser nulo"
            );
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException(
                    "la fecha de actualización no puede ser nula"
            );
        }

        if (criterionResults == null) {
            throw new IllegalArgumentException(
                    "la lista de resultados por criterio no puede ser nula"
            );
        }

        return new Answer(
                id,
                submissionId,
                questionId,
                answerText,
                gradingStatus,
                aiGeneralFeedback,
                aiModel,
                errorMessage,
                reviewedBy,
                reviewedAt,
                updatedAt,
                new ArrayList<>(criterionResults)
        );
    }

    public void updateAnswerText(String answerText) {
        validateAnswerText(answerText);

        this.answerText = answerText;
        this.updatedAt = OffsetDateTime.now();
    }

    public void addCriterionResult(CriterionResult criterionResult) {
        if (criterionResult == null) {
            throw new IllegalArgumentException(
                    "el resultado del criterio no puede ser nulo"
            );
        }

        boolean alreadyEvaluated = criterionResults.stream()
                .anyMatch(existing ->
                        existing.getRubricCriterionId()
                                .equals(criterionResult.getRubricCriterionId())
                );

        if (alreadyEvaluated) {
            throw new IllegalStateException(
                    "ya existe un resultado para este criterio de rúbrica"
            );
        }

        criterionResults.add(criterionResult);
        updatedAt = OffsetDateTime.now();
    }

    public void startGrading() {
        if (gradingStatus != GradingStatus.PENDING) {
            throw new IllegalStateException(
                    "solo una respuesta pendiente puede iniciar su evaluación"
            );
        }

        gradingStatus = GradingStatus.PROCESSING;
        updatedAt = OffsetDateTime.now();
    }

    public void completeGrading(
            String aiGeneralFeedback,
            String aiModel
    ) {
        if (gradingStatus != GradingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "solo una respuesta en procesamiento puede completar su evaluación"
            );
        }

        this.aiGeneralFeedback = aiGeneralFeedback;
        this.aiModel = aiModel;
        this.errorMessage = null;
        this.gradingStatus = GradingStatus.COMPLETED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markGradingError(String errorMessage) {
        if (gradingStatus != GradingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "solo una respuesta en procesamiento puede registrar un error"
            );
        }

        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "el mensaje de error no puede estar vacío"
            );
        }

        this.errorMessage = errorMessage;
        this.gradingStatus = GradingStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markAsReviewed(UUID reviewerId) {
        validateId(
                reviewerId,
                "el id del revisor no puede ser nulo"
        );

        this.reviewedBy = reviewerId;
        this.reviewedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    private static void validateAnswerText(String answerText) {
        if (answerText == null || answerText.isBlank()) {
            throw new IllegalArgumentException(
                    "la respuesta no puede estar vacía"
            );
        }
    }

    private static void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public String getAnswerText() {
        return answerText;
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

    public List<CriterionResult> getCriterionResults() {
        return Collections.unmodifiableList(criterionResults);
    }
}