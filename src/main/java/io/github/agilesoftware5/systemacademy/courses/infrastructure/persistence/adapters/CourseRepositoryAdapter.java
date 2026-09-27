package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.assemblers.CourseAssembler;
import io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.repositories.CourseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CourseRepositoryAdapter implements CourseRepository {

    private final CourseJpaRepository jpaRepository;

    public CourseRepositoryAdapter(CourseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Course save(Course course) {
        var saved = jpaRepository.save(CourseAssembler.toEntity(course));
        return CourseAssembler.toDomain(saved);
    }

    @Override
    public Optional<Course> findById(UUID id) {
        return jpaRepository.findById(id).map(CourseAssembler::toDomain);
    }

    @Override
    public Optional<Course> findByEnrollmentCode(String enrollmentCode) {
        return jpaRepository.findByEnrollmentCode(enrollmentCode).map(CourseAssembler::toDomain);
    }

    @Override
    public List<Course> findByInstructorId(UUID instructorId) {
        return jpaRepository.findByInstructorId(instructorId).stream().map(CourseAssembler::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByEnrollmentCode(String enrollmentCode) {
        return jpaRepository.existsByEnrollmentCode(enrollmentCode);
    }
}
