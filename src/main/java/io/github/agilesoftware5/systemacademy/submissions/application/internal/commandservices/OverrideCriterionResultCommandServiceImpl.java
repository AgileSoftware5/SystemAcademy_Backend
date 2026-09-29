package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.OverrideCriterionResultCommandService;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.OverrideCriterionResultCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OverrideCriterionResultCommandServiceImpl
        implements OverrideCriterionResultCommandService {

    private static final Logger logger =
            LoggerFactory.getLogger(OverrideCriterionResultCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;

    public OverrideCriterionResultCommandServiceImpl(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Result<Submission> handle(OverrideCriterionResultCommand command) {

        var submission = submissionRepository
                .findById(command.submissionId())
                .orElse(null);

        if (submission == null) {
            return Result.failure(
                    ApplicationError.notFound("entrega no encontrada")
            );
        }

        try {
            submission.overrideCriterionResult(
                    command.answerId(),
                    command.criterionResultId(),
                    command.finalVerdict(),
                    command.finalFeedback()
            );

            var savedSubmission = submissionRepository.save(submission);

            logger.info(
                    "Resultado de criterio actualizado: criterionResultId={}, submissionId={}",
                    command.criterionResultId(),
                    command.submissionId()
            );

            return Result.success(savedSubmission);

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError(e.getMessage())
            );

        } catch (IllegalStateException e) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(e.getMessage())
            );
        }
    }
}