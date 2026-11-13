package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.StartSubmissionCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionWindowGuard;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.StartSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StartSubmissionCommandServiceImpl
        implements StartSubmissionCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(StartSubmissionCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final AssessmentAccessProvider assessmentAccess;
    private final SubmissionWindowGuard submissionWindowGuard;

    public StartSubmissionCommandServiceImpl(
            SubmissionRepository submissionRepository,
            AssessmentAccessProvider assessmentAccess,
            SubmissionWindowGuard submissionWindowGuard
    ) {
        this.submissionRepository = submissionRepository;
        this.assessmentAccess = assessmentAccess;
        this.submissionWindowGuard = submissionWindowGuard;
    }

    @Override
    public Result<Submission> handle(StartSubmissionCommand command) {
        log.debug(
                "iniciando entrega assessmentId={} studentId={}",
                command.assessmentId(),
                command.studentId()
        );

        if (!assessmentAccess.isStudentEnrolled(command.assessmentId(), command.studentId())) {
            log.warn(
                    "alumno no matriculado intenta rendir assessmentId={} studentId={}",
                    command.assessmentId(),
                    command.studentId()
            );
            return Result.failure(
                    ApplicationError.forbidden("no estás matriculado en el curso de esta evaluación")
            );
        }

        var window = submissionWindowGuard.ensureOpen(command.assessmentId());
        if (window.isFailure()) {
            log.warn("evaluación fuera de ventana assessmentId={}", command.assessmentId());
            return Result.failure(window.getError());
        }

        if (submissionRepository.existsByAssessmentIdAndStudentId(command.assessmentId(), command.studentId())) {
            log.warn(
                    "segundo intento rechazado assessmentId={} studentId={}",
                    command.assessmentId(),
                    command.studentId()
            );
            return Result.failure(
                    ApplicationError.conflict("ya iniciaste esta evaluación")
            );
        }

        try {
            var saved = submissionRepository.save(
                    Submission.start(command.assessmentId(), command.studentId())
            );

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
