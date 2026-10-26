package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.AnswerEntity;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.assemblers.SubmissionAssembler;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.repositories.SubmissionJpaRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SubmissionRepositoryAdapter implements SubmissionRepository {

    private final SubmissionJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionRepositoryAdapter(
            SubmissionJpaRepository jpaRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Submission save(Submission submission) {

        removeSupersededCriterionResults(submission);

        var saved = jpaRepository.save(
                SubmissionAssembler.toEntity(submission)
        );

        submission.domainEvents().forEach(eventPublisher::publishEvent);
        submission.clearDomainEvents();

        return SubmissionAssembler.toDomain(saved);
    }

    /**
     * Hibernate ejecuta los INSERT antes que los DELETE al hacer flush. Si una respuesta se vuelve a
     * calificar, los resultados nuevos (ids nuevos) chocarían con los viejos en el unique
     * (answer_id, rubric_criterion_id). Se borran primero los que el dominio ya no conserva.
     */
    private void removeSupersededCriterionResults(Submission submission) {
        Set<UUID> kept = submission.getAnswers().stream()
                .flatMap(answer -> answer.getCriterionResults().stream())
                .map(CriterionResult::getId)
                .collect(Collectors.toSet());

        jpaRepository.findById(submission.getId()).ifPresent(existing -> {
            boolean removed = false;
            for (AnswerEntity answer : existing.getAnswers()) {
                removed |= answer.getCriterionResults().removeIf(result -> !kept.contains(result.getId()));
            }
            if (removed) {
                jpaRepository.flush();
            }
        });
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

    @Override
    public long countUngradedAnswersOfSubmitted(UUID assessmentId) {

        return jpaRepository.countAnswersByStatus(
                assessmentId,
                SubmissionStatus.SUBMITTED,
                GradingStatus.COMPLETED
        );
    }

    @Override
    public boolean existsByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId) {

        return jpaRepository.existsByAssessmentIdAndStudentId(assessmentId, studentId);
    }
}
