package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.SubmitSubmissionCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionOwnershipGuard;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.SubmitSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubmitSubmissionCommandServiceImpl
        implements SubmitSubmissionCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(SubmitSubmissionCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final SubmissionOwnershipGuard submissionOwnershipGuard;


    public SubmitSubmissionCommandServiceImpl(
            SubmissionRepository submissionRepository,
            SubmissionOwnershipGuard submissionOwnershipGuard
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionOwnershipGuard = submissionOwnershipGuard;
    }

    @Override
    public Result<Submission> handle(SubmitSubmissionCommand command) {
        var ownershipResult = submissionOwnershipGuard.ensureOwner(
                command.submissionId(),
                command.studentId()
        );

        if (ownershipResult.isFailure()) {
            return Result.failure(ownershipResult.getError());
        }

        log.debug(
                "enviando entrega submissionId={} studentId={}",
                command.submissionId(),
                command.studentId()
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

//        if (!submission.getStudentId().equals(command.studentId())) {
//            log.warn(
//                    "intento de enviar entrega ajena submissionId={} studentId={}",
//                    command.submissionId(),
//                    command.studentId()
//            );
//
//            return Result.failure(
//                    ApplicationError.businessRuleViolation(
//                            "la entrega no pertenece al estudiante"
//                    )
//            );
//        }

        try {
            submission.submit();

            var saved = submissionRepository.save(submission);

            log.info(
                    "entrega enviada id={} studentId={}",
                    saved.getId(),
                    saved.getStudentId()
            );

            return Result.success(saved);

        } catch (IllegalStateException ex) {
            log.warn(
                    "no se pudo enviar la entrega: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }
    }
}
