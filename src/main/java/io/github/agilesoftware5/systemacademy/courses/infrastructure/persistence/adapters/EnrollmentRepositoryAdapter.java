package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.assemblers.EnrollmentAssembler;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.repositories.EnrollmentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final EnrollmentJpaRepository jpaRepository;

    public EnrollmentRepositoryAdapter(EnrollmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        var saved = jpaRepository.save(EnrollmentAssembler.toEntity(enrollment));
        return EnrollmentAssembler.toDomain(saved);
    }

    @Override
    public boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        return jpaRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }
}
