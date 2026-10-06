package io.github.agilesoftware5.systemacademy.submissions.support;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;

import java.util.*;

public class InMemorySubmissionRepository implements SubmissionRepository {

    private final Map<UUID, Submission> store = new HashMap<>();
    public int saves;

    @Override
    public Submission save(Submission submission) {
        saves++;
        store.put(submission.getId(), submission);
        return submission;
    }

    @Override
    public Optional<Submission> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Submission> findByStudentId(UUID studentId) {
        return store.values().stream().filter(s -> s.getStudentId().equals(studentId)).toList();
    }

    @Override
    public List<Submission> findByAssessmentId(UUID assessmentId) {
        return store.values().stream().filter(s -> s.getAssessmentId().equals(assessmentId)).toList();
    }

    @Override
    public long countUngradedAnswersOfSubmitted(UUID assessmentId) {
        return findByAssessmentId(assessmentId).stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED)
                .flatMap(s -> s.getAnswers().stream())
                .filter(a -> a.getGradingStatus() != GradingStatus.COMPLETED)
                .count();
    }

    @Override
    public boolean existsByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId) {
        return store.values().stream()
                .anyMatch(s -> s.getAssessmentId().equals(assessmentId) && s.getStudentId().equals(studentId));
    }
}
