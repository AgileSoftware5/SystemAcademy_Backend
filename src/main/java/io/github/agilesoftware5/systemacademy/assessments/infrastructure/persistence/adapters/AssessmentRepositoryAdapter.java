package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.assemblers.AssessmentAssembler;
import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.repositories.AssessmentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssessmentRepositoryAdapter implements AssessmentRepository {

    private final AssessmentJpaRepository jpaRepository;

    public AssessmentRepositoryAdapter(AssessmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Assessment save(Assessment assessment) {
        var saved = jpaRepository.save(AssessmentAssembler.toEntity(assessment));
        return AssessmentAssembler.toDomain(saved);
    }

    @Override
    public Optional<Assessment> findById(UUID id) {
        return jpaRepository.findById(id).map(AssessmentAssembler::toDomain);
    }

    @Override
    public List<Assessment> findByCourseId(UUID courseId) {
        return jpaRepository.findByCourseId(courseId).stream().map(AssessmentAssembler::toDomain).toList();
    }
}
