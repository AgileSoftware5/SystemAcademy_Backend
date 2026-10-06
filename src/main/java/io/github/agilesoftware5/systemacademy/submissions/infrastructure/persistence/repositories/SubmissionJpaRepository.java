package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.SubmissionEntity;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubmissionJpaRepository
        extends JpaRepository<SubmissionEntity, UUID> {

    List<SubmissionEntity> findByStudentId(UUID studentId);

    List<SubmissionEntity> findByAssessmentId(UUID assessmentId);

    boolean existsByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId);

    @Query("""
            select count(a) from AnswerEntity a
            where a.submission.assessmentId = :assessmentId
              and a.submission.status = :submitted
              and a.gradingStatus <> :completed
            """)
    long countAnswersByStatus(@Param("assessmentId") UUID assessmentId,
                              @Param("submitted") SubmissionStatus submitted,
                              @Param("completed") GradingStatus completed);
}
