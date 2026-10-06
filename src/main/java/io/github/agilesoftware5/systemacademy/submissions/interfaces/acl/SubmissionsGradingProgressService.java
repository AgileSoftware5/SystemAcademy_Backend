package io.github.agilesoftware5.systemacademy.submissions.interfaces.acl;

import io.github.agilesoftware5.systemacademy.assessments.application.internal.outboundservices.GradingProgressService;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Responde al contexto assessments cuánto falta calificar, sin exponerle el modelo de submissions. */
@Component
public class SubmissionsGradingProgressService implements GradingProgressService {

    private final SubmissionRepository submissionRepository;

    public SubmissionsGradingProgressService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public long countUngradedAnswers(UUID assessmentId) {
        return submissionRepository.countUngradedAnswersOfSubmitted(assessmentId);
    }
}
