package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.assemblers.EnrollmentAssembler;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.repositories.EnrollmentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public java.util.Optional<Enrollment> findByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        return jpaRepository.findByCourseIdAndStudentId(courseId, studentId).map(EnrollmentAssembler::toDomain);
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(
            UUID studentId,
            EnrollmentStatus status
    ) {
        return jpaRepository.findByStudentIdAndStatus(studentId, status)
                .stream()
                .map(EnrollmentAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByCourseIdAndStatus(UUID courseId, EnrollmentStatus status) {
        return jpaRepository.findByCourseIdAndStatus(courseId, status)
                .stream()
                .map(EnrollmentAssembler::toDomain)
                .toList();
    }
}
