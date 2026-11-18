package io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.Question;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.valueobjects.AssessmentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 * Aggregate root: Assessment es la frontera de consistencia completa (incluye sus Question).
 * No se puede editar preguntas fuera de este objeto — así se garantizan las invariantes
 * (posiciones correlativas, no se publica sin contenido) dentro de una sola transacción.
 */
public class Assessment {

    private final UUID id;
    private final UUID courseId;
    private String title;
    private String instructions;
    private Instant opensAt;
    private Instant closesAt;
    private AssessmentStatus status;
    private Instant resultsReleasedAt;
    private final List<Question> questions;
    /** Versión para control de concurrencia optimista; null mientras la evaluación no fue persistida. */
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Assessment(UUID id, UUID courseId, String title, String instructions, Instant opensAt, Instant closesAt,
                        AssessmentStatus status, Instant resultsReleasedAt, List<Question> questions, Long version, Instant createdAt,
                       Instant updatedAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.instructions = instructions;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.status = status;
        this.resultsReleasedAt = resultsReleasedAt;
        this.questions = questions;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Alta de una evaluación nueva: nace DRAFT y vacía, el docente le agrega preguntas después. */
    public static Assessment create(UUID courseId, String title, String instructions) {
        Objects.requireNonNull(courseId, "courseId no puede ser null");
        validateTitle(title);
        return new Assessment(UUID.randomUUID(), courseId, title, instructions, null, null,
                AssessmentStatus.DRAFT, null, new ArrayList<>(), null, null, null);
    }

    public static Assessment fromPersistence(UUID id, UUID courseId, String title, String instructions,
                                              Instant opensAt, Instant closesAt, AssessmentStatus status,
                                              Instant resultsReleasedAt, List<Question> questions, Long version, Instant createdAt,
                                             Instant updatedAt) {
        return new Assessment(id, courseId, title, instructions, opensAt, closesAt, status, resultsReleasedAt,
                new ArrayList<>(questions), version, createdAt,
                updatedAt);
    }

    /** Agrega una pregunta abierta al final. Solo permitido mientras la evaluación está en DRAFT. */
    public UUID addOpenQuestion(String stem, String modelAnswer, String explanation, List<String> criteriaDescriptions) {
        ensureDraft();
        int position = questions.size() + 1;
        Question question = Question.openQuestion(position, stem, modelAnswer, explanation, criteriaDescriptions);
        questions.add(question);
        return question.getId();
    }

    /**
     * Edita título, instrucciones y ventana de fechas. Solo permitido mientras la evaluación está en DRAFT.
     * Las fechas son opcionales en borrador (se pueden definir recién al publicar), pero si vienen ambas
     * deben ser coherentes.
     */
    public void updateDraft(String title, String instructions, Instant opensAt, Instant closesAt) {
        ensureDraft();
        validateTitle(title);
        validateScheduleOrder(opensAt, closesAt);
        this.title = title;
        this.instructions = instructions;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    /**
     * Publica la evaluación: a partir de acá los alumnos pueden rendirla y ya no se pueden agregar preguntas.
     * Las fechas recibidas tienen prioridad; si faltan se usan las que ya se guardaron en el borrador.
     */
    public void publish(Instant opensAt, Instant closesAt) {
        ensureDraft();
        if (questions.isEmpty()) {
            throw new IllegalStateException("no se puede publicar una evaluación sin preguntas");
        }
        Instant effectiveOpensAt = opensAt != null ? opensAt : this.opensAt;
        Instant effectiveClosesAt = closesAt != null ? closesAt : this.closesAt;
        if (effectiveOpensAt == null || effectiveClosesAt == null) {
            throw new IllegalArgumentException("opensAt y closesAt son obligatorios para publicar");
        }
        validateScheduleOrder(effectiveOpensAt, effectiveClosesAt);
        this.opensAt = effectiveOpensAt;
        this.closesAt = effectiveClosesAt;
        this.status = AssessmentStatus.PUBLISHED;
    }

    public void close() {
        if (status != AssessmentStatus.PUBLISHED) {
            throw new IllegalStateException("solo una evaluación publicada puede cerrarse");
        }
        this.status = AssessmentStatus.CLOSED;
    }

    /** Hace visibles las calificaciones a los alumnos. Solo cuando ya nadie puede seguir rindiendo. */
    public void releaseResults(Instant now) {
        Objects.requireNonNull(now, "now no puede ser null");
        if (areResultsReleased()) {
            throw new IllegalStateException("los resultados ya fueron publicados");
        }
        boolean closed = status == AssessmentStatus.CLOSED
                || (status == AssessmentStatus.PUBLISHED && now.isAfter(closesAt));
        if (!closed) {
            throw new IllegalStateException("solo se publican resultados de una evaluación ya cerrada");
        }
        this.resultsReleasedAt = now;
    }

    public boolean areResultsReleased() {
        return resultsReleasedAt != null;
    }

    /** Hecho puro sobre esta evaluación: no decide qué pasa si no está abierta, eso es responsabilidad de quien la rinde. */
    public boolean isOpenForSubmissions(Instant now) {
        return status == AssessmentStatus.PUBLISHED
                && !now.isBefore(opensAt)
                && !now.isAfter(closesAt);
    }

    private void ensureDraft() {
        if (status != AssessmentStatus.DRAFT) {
            throw new IllegalStateException("la evaluación ya no está en borrador");
        }
    }

    /** Regla de ventana: si hay ambas fechas, el cierre es estrictamente posterior a la apertura. */
    private static void validateScheduleOrder(Instant opensAt, Instant closesAt) {
        if (opensAt != null && closesAt != null && !closesAt.isAfter(opensAt)) {
            throw new IllegalArgumentException("closesAt debe ser posterior a opensAt");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title no puede estar vacío");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public Instant getOpensAt() {
        return opensAt;
    }

    public Instant getClosesAt() {
        return closesAt;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public Instant getResultsReleasedAt() {
        return resultsReleasedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
