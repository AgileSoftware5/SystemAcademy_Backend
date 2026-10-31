package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, UUID> {

    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);

    List<EnrollmentEntity> findByStudentIdAndStatus(
            UUID studentId,
            EnrollmentStatus status
    );
}
