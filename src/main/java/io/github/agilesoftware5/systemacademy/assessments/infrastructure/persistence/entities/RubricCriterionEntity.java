package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Sin auditoría: rubric_criteria en el ER solo tiene id/question_id/position/description. */
@Entity
@Table(name = "rubric_criteria")
public class RubricCriterionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    protected RubricCriterionEntity() {
        // requerido por JPA
    }

    public RubricCriterionEntity(UUID id, QuestionEntity question, int position, String description) {
        this.id = id;
        this.question = question;
        this.position = position;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }
}
