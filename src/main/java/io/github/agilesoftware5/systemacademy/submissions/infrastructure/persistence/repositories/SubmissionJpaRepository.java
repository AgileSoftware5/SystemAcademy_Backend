package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionJpaRepository
        extends JpaRepository<SubmissionEntity, UUID> {

    List<SubmissionEntity> findByStudentId(UUID studentId);

    List<SubmissionEntity> findByAssessmentId(UUID assessmentId);
}
