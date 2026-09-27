package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.valueobjects.AssessmentStatus;
import io.github.agilesoftware5.systemacademy.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Aggregate completo persistido como un solo grafo: assessment + questions + criteria en cascada. */
@Entity
@Table(name = "assessments")
public class AssessmentEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String instructions;

    @Column(name = "opens_at")
    private Instant opensAt;

    @Column(name = "closes_at")
    private Instant closesAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentStatus status;

    @Column(name = "results_released_at")
    private Instant resultsReleasedAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<QuestionEntity> questions = new ArrayList<>();

    protected AssessmentEntity() {
        // requerido por JPA
    }

    public AssessmentEntity(UUID id, UUID courseId, String title, String instructions, Instant opensAt,
                             Instant closesAt, AssessmentStatus status, Instant resultsReleasedAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.instructions = instructions;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.status = status;
        this.resultsReleasedAt = resultsReleasedAt;
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

    public List<QuestionEntity> getQuestions() {
        return questions;
    }
}
