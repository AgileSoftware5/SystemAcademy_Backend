package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.OverrideCriterionResultCommandService;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.OverrideCriterionResultCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OverrideCriterionResultCommandServiceImpl
        implements OverrideCriterionResultCommandService {

    private static final Logger logger =
            LoggerFactory.getLogger(OverrideCriterionResultCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final AssessmentAccessProvider assessmentAccess;

    public OverrideCriterionResultCommandServiceImpl(
            SubmissionRepository submissionRepository,
            AssessmentAccessProvider assessmentAccess
    ) {
        this.submissionRepository = submissionRepository;
        this.assessmentAccess = assessmentAccess;
    }

    @Override
    public Result<Submission> handle(OverrideCriterionResultCommand command) {

        var submission = submissionRepository
                .findById(command.submissionId())
                .orElse(null);

        if (submission == null) {
            logger.warn("entrega no encontrada submissionId={}", command.submissionId());
            return Result.failure(
                    ApplicationError.notFound("entrega no encontrada")
            );
        }

        if (!assessmentAccess.isTaughtBy(submission.getAssessmentId(), command.reviewerId())) {
            logger.warn("intento de revisar entrega ajena submissionId={} reviewerId={}",
                    submission.getId(), command.reviewerId());
            return Result.failure(
                    ApplicationError.forbidden("no sos el docente dueño de esta evaluación")
            );
        }

        try {
            submission.overrideCriterionResult(
                    command.answerId(),
                    command.criterionResultId(),
                    command.finalVerdict(),
                    command.finalFeedback(),
                    command.reviewerId()
            );

            var savedSubmission = submissionRepository.save(submission);

            logger.info(
                    "resultado de criterio revisado criterionResultId={} submissionId={} reviewerId={} finalVerdict={}",
                    command.criterionResultId(),
                    command.submissionId(),
                    command.reviewerId(),
                    command.finalVerdict()
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