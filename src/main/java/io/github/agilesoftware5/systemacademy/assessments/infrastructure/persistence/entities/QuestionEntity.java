package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class QuestionEntity extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private AssessmentEntity assessment;

    @Column(nullable = false)
    private int position;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType = "OPEN";

    @Column(nullable = false, columnDefinition = "text")
    private String stem;

    @Column(name = "model_answer", columnDefinition = "text")
    private String modelAnswer;

    @Column(columnDefinition = "text")
    private String explanation;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<RubricCriterionEntity> criteria = new ArrayList<>();

    protected QuestionEntity() {
        // requerido por JPA
    }

    public QuestionEntity(UUID id, AssessmentEntity assessment, int position, String stem, String modelAnswer, String explanation) {
        this.id = id;
        this.assessment = assessment;
        this.position = position;
        this.stem = stem;
        this.modelAnswer = modelAnswer;
        this.explanation = explanation;
    }

    public UUID getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getStem() {
        return stem;
    }

    public String getModelAnswer() {
        return modelAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<RubricCriterionEntity> getCriteria() {
        return criteria;
    }
}
