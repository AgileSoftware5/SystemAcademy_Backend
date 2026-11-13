package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, UUID> {

    Optional<CourseEntity> findByEnrollmentCode(String enrollmentCode);

    List<CourseEntity> findByInstructorId(UUID instructorId);

    boolean existsByCode(String code);

    boolean existsByEnrollmentCode(String enrollmentCode);
}
