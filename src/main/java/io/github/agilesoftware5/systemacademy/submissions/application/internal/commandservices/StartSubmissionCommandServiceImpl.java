package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.StartSubmissionCommandService;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.StartSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StartSubmissionCommandServiceImpl
        implements StartSubmissionCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(StartSubmissionCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;

    public StartSubmissionCommandServiceImpl(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Result<Submission> handle(StartSubmissionCommand command) {

        log.debug(
                "iniciando entrega assessmentId={} studentId={}",
                command.assessmentId(),
                command.studentId()
        );

        try {
            var submission = Submission.start(
                    command.assessmentId(),
                    command.studentId()
            );

            var saved = submissionRepository.save(submission);

            log.info(
                    "entrega iniciada id={} assessmentId={} studentId={}",
                    saved.getId(),
                    saved.getAssessmentId(),
                    saved.getStudentId()
            );

            return Result.success(saved);

        } catch (IllegalArgumentException ex) {
            log.warn(
                    "datos inválidos para iniciar entrega: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.validationError(ex.getMessage())
            );
        }
    }
}
