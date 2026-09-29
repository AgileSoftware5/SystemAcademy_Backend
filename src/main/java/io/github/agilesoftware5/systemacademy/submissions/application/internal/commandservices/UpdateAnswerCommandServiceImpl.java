package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.UpdateAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionOwnershipGuard;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.UpdateAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateAnswerCommandServiceImpl
        implements UpdateAnswerCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(UpdateAnswerCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final SubmissionOwnershipGuard submissionOwnershipGuard;


    public UpdateAnswerCommandServiceImpl(
            SubmissionRepository submissionRepository,
            SubmissionOwnershipGuard submissionOwnershipGuard
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionOwnershipGuard = submissionOwnershipGuard;
    }

    @Override
    public Result<Submission> handle(UpdateAnswerCommand command) {
        var ownershipResult = submissionOwnershipGuard.ensureOwner(
                command.submissionId(),
                command.studentId()
        );

        if (ownershipResult.isFailure()) {
            return Result.failure(ownershipResult.getError());
        }

        log.debug(
                "actualizando respuesta submissionId={} answerId={}",
                command.submissionId(),
                command.answerId()
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

        try {
            submission.updateAnswer(
                    command.answerId(),
                    command.answerText()
            );

            var saved = submissionRepository.save(submission);

            log.info(
                    "respuesta actualizada answerId={} submissionId={}",
                    command.answerId(),
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
                    "no se pudo actualizar respuesta: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }
    }
}
