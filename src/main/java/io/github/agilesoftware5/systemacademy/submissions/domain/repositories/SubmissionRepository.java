package io.github.agilesoftware5.systemacademy.submissions.domain.repositories;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {

    Submission save(Submission submission);

    Optional<Submission> findById(UUID id);

    List<Submission> findByStudentId(UUID studentId);

    List<Submission> findByAssessmentId(UUID assessmentId);
}
