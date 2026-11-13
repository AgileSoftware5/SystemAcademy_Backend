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
    private final String title;
    private final String instructions;
    private Instant opensAt;
    private Instant closesAt;
    private AssessmentStatus status;
    private Instant resultsReleasedAt;
    private final List<Question> questions;

    private Assessment(UUID id, UUID courseId, String title, String instructions, Instant opensAt, Instant closesAt,
                        AssessmentStatus status, Instant resultsReleasedAt, List<Question> questions) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.instructions = instructions;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.status = status;
        this.resultsReleasedAt = resultsReleasedAt;
        this.questions = questions;
    }

    /** Alta de una evaluación nueva: nace DRAFT y vacía, el docente le agrega preguntas después. */
    public static Assessment create(UUID courseId, String title, String instructions) {
        Objects.requireNonNull(courseId, "courseId no puede ser null");
        validateTitle(title);
        return new Assessment(UUID.randomUUID(), courseId, title, instructions, null, null,
                AssessmentStatus.DRAFT, null, new ArrayList<>());
    }

    public static Assessment fromPersistence(UUID id, UUID courseId, String title, String instructions,
                                              Instant opensAt, Instant closesAt, AssessmentStatus status,
                                              Instant resultsReleasedAt, List<Question> questions) {
        return new Assessment(id, courseId, title, instructions, opensAt, closesAt, status, resultsReleasedAt, new ArrayList<>(questions));
    }

    /** Agrega una pregunta abierta al final. Solo permitido mientras la evaluación está en DRAFT. */
    public UUID addOpenQuestion(String stem, String modelAnswer, String explanation, List<String> criteriaDescriptions) {
        ensureDraft();
        int position = questions.size() + 1;
        Question question = Question.openQuestion(position, stem, modelAnswer, explanation, criteriaDescriptions);
        questions.add(question);
        return question.getId();
    }

    /** Publica la evaluación: a partir de acá los alumnos pueden rendirla y ya no se pueden agregar preguntas. */
    public void publish(Instant opensAt, Instant closesAt) {
        ensureDraft();
        if (questions.isEmpty()) {
            throw new IllegalStateException("no se puede publicar una evaluación sin preguntas");
        }
        Objects.requireNonNull(opensAt, "opensAt no puede ser null");
        Objects.requireNonNull(closesAt, "closesAt no puede ser null");
        if (!closesAt.isAfter(opensAt)) {
            throw new IllegalArgumentException("closesAt debe ser posterior a opensAt");
        }
        this.opensAt = opensAt;
        this.closesAt = closesAt;
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

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
}
