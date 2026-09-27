package io.github.agilesoftware5.systemacademy.assessments.domain.repositories;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository {

    Assessment save(Assessment assessment);

    Optional<Assessment> findById(UUID id);

    List<Assessment> findByCourseId(UUID courseId);
}
