package io.github.agilesoftware5.systemacademy.courses.domain.repositories;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(UUID id);

    Optional<Course> findByEnrollmentCode(String enrollmentCode);

    List<Course> findByInstructorId(UUID instructorId);

    boolean existsByCode(String code);

    boolean existsByEnrollmentCode(String enrollmentCode);
}
