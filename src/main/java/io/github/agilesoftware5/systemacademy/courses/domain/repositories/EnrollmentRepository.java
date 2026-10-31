package io.github.agilesoftware5.systemacademy.courses.domain.repositories;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);

    List<Enrollment> findByStudentIdAndStatus(
            UUID studentId,
            EnrollmentStatus status
    );
}
