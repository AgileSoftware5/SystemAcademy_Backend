package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.GradeAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.GradeAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionGradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.QuestionGradingDataProvider;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.springframework.stereotype.Service;

@Service
public class GradeAnswerCommandServiceImpl
        implements GradeAnswerCommandService {

    private final SubmissionRepository submissionRepository;
    private final QuestionGradingDataProvider questionGradingDataProvider;
    private final GradingEngine gradingEngine;

    public GradeAnswerCommandServiceImpl(
            SubmissionRepository submissionRepository,
            QuestionGradingDataProvider questionGradingDataProvider,
            GradingEngine gradingEngine
    ) {
        this.submissionRepository = submissionRepository;
        this.questionGradingDataProvider = questionGradingDataProvider;
        this.gradingEngine = gradingEngine;
    }

    @Override
    public Result<Submission> handle(GradeAnswerCommand command) {

        var submission = submissionRepository
                .findById(command.submissionId())
                .orElse(null);

        if (submission == null) {
            return Result.failure(
                    ApplicationError.notFound("entrega no encontrada")
            );
        }

        try {

            Answer answer = submission.getAnswers()
                    .stream()
                    .filter(existing ->
                            existing.getId().equals(command.answerId())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "la respuesta no pertenece a esta entrega"
                            )
                    );

            answer.startGrading();

            var input = questionGradingDataProvider.getQuestionGradingInput(
                    answer.getQuestionId(),
                    answer.getAnswerText()
            );

            GradingResult gradingResult =
                    gradingEngine.grade(input);

            for (CriterionGradingResult criterionResult :
                    gradingResult.criteria()) {

                answer.addCriterionResult(
                        CriterionResult.create(
                                answer.getId(),
                                criterionResult.rubricCriterionId(),
                                criterionResult.verdict(),
                                criterionResult.evidence(),
                                criterionResult.feedback()
                        )
                );
            }

            answer.completeGrading(
                    gradingResult.generalFeedback(),
                    gradingResult.model()
            );

            var savedSubmission =
                    submissionRepository.save(submission);

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