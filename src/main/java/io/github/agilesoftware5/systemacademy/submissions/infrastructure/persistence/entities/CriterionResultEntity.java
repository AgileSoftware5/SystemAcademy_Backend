package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "criterion_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_criterion_result_answer_criterion",
                        columnNames = {"answer_id", "rubric_criterion_id"}
                )
        }
)
public class CriterionResultEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private AnswerEntity answer;

    @Column(name = "rubric_criterion_id", nullable = false)
    private UUID rubricCriterionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_verdict", nullable = false, length = 30)
    private CriterionVerdict aiVerdict;

    @Column(name = "ai_evidence", columnDefinition = "text")
    private String aiEvidence;

    @Column(name = "ai_feedback", columnDefinition = "text")
    private String aiFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_verdict", nullable = false, length = 30)
    private CriterionVerdict finalVerdict;

    @Column(name = "final_feedback", columnDefinition = "text")
    private String finalFeedback;

    /** Orden del criterio en la rúbrica. Null en filas anteriores a esta columna. */
    @Column(name = "position")
    private Integer position;

    protected CriterionResultEntity() {
    }

    public CriterionResultEntity(
            UUID id,
            AnswerEntity answer,
            UUID rubricCriterionId,
            CriterionVerdict aiVerdict,
            String aiEvidence,
            String aiFeedback,
            CriterionVerdict finalVerdict,
            String finalFeedback,
            Integer position
    ) {
        this.id = id;
        this.answer = answer;
        this.rubricCriterionId = rubricCriterionId;
        this.aiVerdict = aiVerdict;
        this.aiEvidence = aiEvidence;
        this.aiFeedback = aiFeedback;
        this.finalVerdict = finalVerdict;
        this.finalFeedback = finalFeedback;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public AnswerEntity getAnswer() {
        return answer;
    }

    public UUID getRubricCriterionId() {
        return rubricCriterionId;
    }

    public CriterionVerdict getAiVerdict() {
        return aiVerdict;
    }

    public String getAiEvidence() {
        return aiEvidence;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public CriterionVerdict getFinalVerdict() {
        return finalVerdict;
    }

    public String getFinalFeedback() {
        return finalFeedback;
    }

    public Integer getPosition() {
        return position;
    }
}
