package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities.CourseEntity;

public final class CourseAssembler {

    private CourseAssembler() {
    }

    public static Course toDomain(CourseEntity entity) {
        return Course.fromPersistence(
                entity.getId(),
                entity.getInstructorId(),
                entity.getCreatedBy(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getEnrollmentCode(),
                entity.getStatus()
        );
    }

    public static CourseEntity toEntity(Course course) {
        return new CourseEntity(
                course.getId(),
                course.getInstructorId(),
                course.getCreatedBy(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                course.getEnrollmentCode(),
                course.getStatus()
        );
    }
}
