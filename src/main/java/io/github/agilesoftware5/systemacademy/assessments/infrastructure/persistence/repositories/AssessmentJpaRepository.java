package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities.AssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentJpaRepository extends JpaRepository<AssessmentEntity, UUID> {

    List<AssessmentEntity> findByCourseId(UUID courseId);
}
