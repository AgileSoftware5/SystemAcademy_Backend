package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.AddAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionOwnershipGuard;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionWindowGuard;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.AddAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AddAnswerCommandServiceImpl
        implements AddAnswerCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(AddAnswerCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final SubmissionOwnershipGuard submissionOwnershipGuard;
    private final SubmissionWindowGuard submissionWindowGuard;
    private final AssessmentAccessProvider assessmentAccess;

    public AddAnswerCommandServiceImpl(
            SubmissionRepository submissionRepository,
            SubmissionOwnershipGuard submissionOwnershipGuard,
            SubmissionWindowGuard submissionWindowGuard,
            AssessmentAccessProvider assessmentAccess
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionOwnershipGuard = submissionOwnershipGuard;
        this.submissionWindowGuard = submissionWindowGuard;
        this.assessmentAccess = assessmentAccess;
    }

    @Override
    public Result<Submission> handle(AddAnswerCommand command) {
        var ownershipResult = submissionOwnershipGuard.ensureOwner(
                command.submissionId(),
                command.studentId()
        );

        if (ownershipResult.isFailure()) {
            return Result.failure(ownershipResult.getError());
        }

        log.debug(
                "agregando respuesta submissionId={} questionId={}",
                command.submissionId(),
                command.questionId()
        );

        var submission = submissionRepository
                .findById(command.submissionId())
                .orElse(null);

        if (submission == null) {
            log.warn(
                    "entrega no encontrada id={}",
                    command.submissionId()
            );

            return Result.failure(
                    ApplicationError.notFound("entrega no encontrada")
            );
        }

        var window = submissionWindowGuard.ensureOpen(submission.getAssessmentId());
        if (window.isFailure()) {
            log.warn(
                    "evaluación cerrada submissionId={} assessmentId={}",
                    submission.getId(),
                    submission.getAssessmentId()
            );
            return Result.failure(window.getError());
        }

        if (!assessmentAccess.hasQuestion(submission.getAssessmentId(), command.questionId())) {
            log.warn(
                    "pregunta ajena a la evaluación submissionId={} questionId={}",
                    submission.getId(),
                    command.questionId()
            );
            return Result.failure(
                    ApplicationError.validationError("la pregunta no pertenece a esta evaluación")
            );
        }

        try {
            var answer = Answer.create(
                    submission.getId(),
                    command.questionId(),
                    command.answerText()
            );

            submission.addAnswer(answer);

            var saved = submissionRepository.save(submission);

            log.info(
                    "respuesta agregada answerId={} submissionId={}",
                    answer.getId(),
                    saved.getId()
            );

            return Result.success(saved);

        } catch (IllegalArgumentException ex) {
            log.warn(
                    "datos de respuesta inválidos: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.validationError(ex.getMessage())
            );

        } catch (IllegalStateException ex) {
            log.warn(
                    "no se pudo agregar respuesta: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }
    }
}
