package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities.EnrollmentEntity;

public final class EnrollmentAssembler {

    private EnrollmentAssembler() {
    }

    public static Enrollment toDomain(EnrollmentEntity entity) {
        return Enrollment.fromPersistence(
                entity.getId(),
                entity.getCourseId(),
                entity.getStudentId(),
                entity.getEnrolledAt(),
                entity.getStatus()
        );
    }

    public static EnrollmentEntity toEntity(Enrollment enrollment) {
        return new EnrollmentEntity(
                enrollment.getId(),
                enrollment.getCourseId(),
                enrollment.getStudentId(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}
