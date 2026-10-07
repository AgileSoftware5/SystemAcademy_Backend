package io.github.agilesoftware5.systemacademy.submissions.application.internal.grading;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionGradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.RubricCriterionInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.QuestionGradingDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Califica una respuesta con el motor de IA y persiste el resultado o el error.
 * No decide quién puede pedirlo: eso lo resuelve cada caso de uso que lo invoca.
 */
@Component
public class AnswerGrader {

    private static final Logger log = LoggerFactory.getLogger(AnswerGrader.class);

    private final SubmissionRepository submissionRepository;
    private final QuestionGradingDataProvider questionGradingDataProvider;
    private final GradingEngine gradingEngine;

    public AnswerGrader(
            SubmissionRepository submissionRepository,
            QuestionGradingDataProvider questionGradingDataProvider,
            GradingEngine gradingEngine
    ) {
        this.submissionRepository = submissionRepository;
        this.questionGradingDataProvider = questionGradingDataProvider;
        this.gradingEngine = gradingEngine;
    }

    public Result<Submission> grade(Submission submission, UUID answerId) {

        Answer answer = submission.getAnswers()
                .stream()
                .filter(existing -> existing.getId().equals(answerId))
                .findFirst()
                .orElse(null);

        if (answer == null) {
            log.warn("respuesta ajena a la entrega submissionId={} answerId={}", submission.getId(), answerId);
            return Result.failure(
                    ApplicationError.validationError("la respuesta no pertenece a esta entrega")
            );
        }

        boolean isRetry = answer.getGradingStatus() == GradingStatus.FAILED;
        long startedAt = System.currentTimeMillis();

        try {
            answer.startGrading();
        } catch (IllegalStateException e) {
            log.warn("calificación rechazada answerId={} status={}: {}",
                    answer.getId(), answer.getGradingStatus(), e.getMessage());
            return Result.failure(
                    ApplicationError.businessRuleViolation(e.getMessage())
            );
        }

        try {

            log.info("calificando respuesta submissionId={} answerId={} questionId={} retry={}",
                    submission.getId(), answer.getId(), answer.getQuestionId(), isRetry);

            QuestionGradingInput input = questionGradingDataProvider.getQuestionGradingInput(
                    submission.getAssessmentId(),
                    answer.getQuestionId(),
                    answer.getAnswerText()
            );

            GradingResult gradingResult = gradingEngine.grade(input);

            for (CriterionGradingResult criterionResult : inRubricOrder(gradingResult.criteria(), input.criteria())) {
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

            var savedSubmission = submissionRepository.save(submission);

            log.info("respuesta calificada answerId={} model={} criteria={} elapsedMs={}",
                    answer.getId(), gradingResult.model(), gradingResult.criteria().size(),
                    System.currentTimeMillis() - startedAt);

            return Result.success(savedSubmission);

        } catch (Exception e) {

            log.error("fallo al calificar answerId={} elapsedMs={}",
                    answer.getId(), System.currentTimeMillis() - startedAt, e);

            answer.markGradingError(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "error inesperado durante la evaluación"
            );

            submissionRepository.save(submission);

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "ocurrió un error durante la evaluación"
                    )
            );
        }
    }

    /** El motor puede devolver los criterios en cualquier orden; se guardan en el de la rúbrica. */
    private static List<CriterionGradingResult> inRubricOrder(
            List<CriterionGradingResult> results,
            List<RubricCriterionInput> rubric
    ) {
        Map<UUID, Integer> positionById = new HashMap<>();
        for (int i = 0; i < rubric.size(); i++) {
            positionById.put(rubric.get(i).rubricCriterionId(), i);
        }
        return results.stream()
                .sorted(Comparator.comparingInt(
                        result -> positionById.getOrDefault(result.rubricCriterionId(), Integer.MAX_VALUE)))
                .toList();
    }
}
