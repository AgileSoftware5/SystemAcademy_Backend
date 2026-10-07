package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.AnswerEntity;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.CriterionResultEntity;
import io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.entities.SubmissionEntity;

import java.util.List;

public final class SubmissionAssembler {

    private SubmissionAssembler() {
    }

    /**
     * Traduce el aggregate Submission completo
     * (con sus Answer y CriterionResult) hacia el dominio.
     */
    public static Submission toDomain(SubmissionEntity entity) {

        List<Answer> answers = entity.getAnswers().stream()
                .map(SubmissionAssembler::answerToDomain)
                .toList();

        return Submission.fromPersistence(
                entity.getId(),
                entity.getAssessmentId(),
                entity.getStudentId(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getSubmittedAt(),
                answers
        );
    }

    private static Answer answerToDomain(AnswerEntity entity) {

        List<CriterionResult> criterionResults =
                entity.getCriterionResults().stream()
                        .map(SubmissionAssembler::criterionResultToDomain)
                        .toList();

        return Answer.fromPersistence(
                entity.getId(),
                entity.getSubmission().getId(),
                entity.getQuestionId(),
                entity.getAnswerText(),
                entity.getGradingStatus(),
                entity.getAiGeneralFeedback(),
                entity.getAiModel(),
                entity.getErrorMessage(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getUpdatedAt(),
                criterionResults
        );
    }

    private static CriterionResult criterionResultToDomain(
            CriterionResultEntity entity
    ) {
        return CriterionResult.fromPersistence(
                entity.getId(),
                entity.getAnswer().getId(),
                entity.getRubricCriterionId(),
                entity.getAiVerdict(),
                entity.getAiEvidence(),
                entity.getAiFeedback(),
                entity.getFinalVerdict(),
                entity.getFinalFeedback()
        );
    }

    /**
     * Traduce el aggregate Submission completo
     * hacia el grafo de entidades JPA.
     */
    public static SubmissionEntity toEntity(Submission submission) {

        SubmissionEntity entity = new SubmissionEntity(
                submission.getId(),
                submission.getAssessmentId(),
                submission.getStudentId(),
                submission.getStatus(),
                submission.getStartedAt(),
                submission.getSubmittedAt()
        );

        for (Answer answer : submission.getAnswers()) {
            entity.getAnswers().add(
                    answerToEntity(answer, entity)
            );
        }

        return entity;
    }

    private static AnswerEntity answerToEntity(
            Answer answer,
            SubmissionEntity parent
    ) {
        AnswerEntity entity = new AnswerEntity(
                answer.getId(),
                parent,
                answer.getQuestionId(),
                null,
                answer.getAnswerText(),
                null,
                answer.getGradingStatus(),
                answer.getAiGeneralFeedback(),
                answer.getAiModel(),
                answer.getErrorMessage(),
                answer.getReviewedBy(),
                answer.getReviewedAt(),
                answer.getUpdatedAt()
        );

        List<CriterionResult> criterionResults = answer.getCriterionResults();
        for (int position = 0; position < criterionResults.size(); position++) {
            entity.getCriterionResults().add(
                    criterionResultToEntity(
                            criterionResults.get(position),
                            entity,
                            position
                    )
            );
        }

        return entity;
    }

    private static CriterionResultEntity criterionResultToEntity(
            CriterionResult criterionResult,
            AnswerEntity parent,
            int position
    ) {
        return new CriterionResultEntity(
                criterionResult.getId(),
                parent,
                criterionResult.getRubricCriterionId(),
                criterionResult.getAiVerdict(),
                criterionResult.getAiEvidence(),
                criterionResult.getAiFeedback(),
                criterionResult.getFinalVerdict(),
                criterionResult.getFinalFeedback(),
                position
        );
    }
}
