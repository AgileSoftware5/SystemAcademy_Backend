package io.github.agilesoftware5.systemacademy.courses.domain.repositories;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;

import java.util.UUID;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);
}
