package io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Submission {

    private final UUID id;
    private final UUID assessmentId;
    private final UUID studentId;

    private SubmissionStatus status;
    private final OffsetDateTime startedAt;
    private OffsetDateTime submittedAt;

    private final List<Answer> answers;

    private Submission(
            UUID id,
            UUID assessmentId,
            UUID studentId,
            SubmissionStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime submittedAt,
            List<Answer> answers
    ) {
        this.id = id;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.status = status;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
        this.answers = answers;
    }

    public static Submission start(UUID assessmentId, UUID studentId) {
        validateId(assessmentId, "el id de la evaluación no puede ser nulo");
        validateId(studentId, "el id del estudiante no puede ser nulo");

        return new Submission(
                UUID.randomUUID(),
                assessmentId,
                studentId,
                SubmissionStatus.IN_PROGRESS,
                OffsetDateTime.now(),
                null,
                new ArrayList<>()
        );
    }

    public static Submission fromPersistence(
            UUID id,
            UUID assessmentId,
            UUID studentId,
            SubmissionStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime submittedAt,
            List<Answer> answers
    ) {
        validateId(id, "el id de la entrega no puede ser nulo");
        validateId(assessmentId, "el id de la evaluación no puede ser nulo");
        validateId(studentId, "el id del estudiante no puede ser nulo");

        if (status == null) {
            throw new IllegalArgumentException(
                    "el estado de la entrega no puede ser nulo"
            );
        }

        if (startedAt == null) {
            throw new IllegalArgumentException(
                    "la fecha de inicio no puede ser nula"
            );
        }

        if (answers == null) {
            throw new IllegalArgumentException(
                    "la lista de respuestas no puede ser nula"
            );
        }

        validateState(status, submittedAt);

        return new Submission(
                id,
                assessmentId,
                studentId,
                status,
                startedAt,
                submittedAt,
                new ArrayList<>(answers)
        );
    }

    public void addAnswer(Answer answer) {
        if (answer == null) {
            throw new IllegalArgumentException(
                    "la respuesta no puede ser nula"
            );
        }

        if (status != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "no se pueden agregar respuestas a una entrega enviada"
            );
        }

        boolean alreadyAnswered = answers.stream()
                .anyMatch(existing ->
                        existing.getQuestionId().equals(answer.getQuestionId())
                );

        if (alreadyAnswered) {
            throw new IllegalStateException(
                    "ya existe una respuesta para esta pregunta"
            );
        }

        answers.add(answer);
    }

    public void submit() {
        if (status != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "solo una entrega en progreso puede ser enviada"
            );
        }

        status = SubmissionStatus.SUBMITTED;
        submittedAt = OffsetDateTime.now();
    }

    private static void validateState(
            SubmissionStatus status,
            OffsetDateTime submittedAt
    ) {
        if (status == SubmissionStatus.SUBMITTED && submittedAt == null) {
            throw new IllegalArgumentException(
                    "una entrega enviada debe tener fecha de envío"
            );
        }

        if (status == SubmissionStatus.IN_PROGRESS && submittedAt != null) {
            throw new IllegalArgumentException(
                    "una entrega en progreso no puede tener fecha de envío"
            );
        }
    }

    private static void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public void updateAnswer(UUID answerId, String answerText) {
        if (status != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "no se pueden modificar respuestas de una entrega enviada"
            );
        }

        Answer answer = answers.stream()
                .filter(existing -> existing.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "la respuesta no pertenece a esta entrega"
                ));

        answer.updateAnswerText(answerText);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<Answer> getAnswers() {
        return Collections.unmodifiableList(answers);
    }
}
