package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_submission_assessment_student",
                columnNames = {"assessment_id", "student_id"}
        )
)
public class SubmissionEntity {

    @Id
    private UUID id;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @OneToMany(
            mappedBy = "submission",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<AnswerEntity> answers = new ArrayList<>();

    protected SubmissionEntity() {
    }

    public SubmissionEntity(
            UUID id,
            UUID assessmentId,
            UUID studentId,
            SubmissionStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime submittedAt
    ) {
        this.id = id;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.status = status;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
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

    public List<AnswerEntity> getAnswers() {
        return answers;
    }
}
