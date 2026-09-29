package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.assemblers.SubmissionAssembler;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.repositories.SubmissionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SubmissionRepositoryAdapter implements SubmissionRepository {

    private final SubmissionJpaRepository jpaRepository;

    public SubmissionRepositoryAdapter(
            SubmissionJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Submission save(Submission submission) {

        var saved = jpaRepository.save(
                SubmissionAssembler.toEntity(submission)
        );

        return SubmissionAssembler.toDomain(saved);
    }

    @Override
    public Optional<Submission> findById(UUID id) {

        return jpaRepository.findById(id)
                .map(SubmissionAssembler::toDomain);
    }

    @Override
    public List<Submission> findByStudentId(UUID studentId) {

        return jpaRepository.findByStudentId(studentId)
                .stream()
                .map(SubmissionAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Submission> findByAssessmentId(UUID assessmentId) {

        return jpaRepository.findByAssessmentId(assessmentId)
                .stream()
                .map(SubmissionAssembler::toDomain)
                .toList();
    }
}
