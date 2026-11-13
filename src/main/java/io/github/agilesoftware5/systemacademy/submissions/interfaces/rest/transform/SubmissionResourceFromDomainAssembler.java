package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.AnswerResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.CriterionResultResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.SubmissionResource;

import java.util.List;

public final class SubmissionResourceFromDomainAssembler {

    private SubmissionResourceFromDomainAssembler() {
    }

    public static SubmissionResource toResource(
            Submission submission,
            GradingVisibility visibility
    ) {
        var answers = submission.getAnswers()
                .stream()
                .map(answer -> toResource(answer, visibility))
                .toList();

        return new SubmissionResource(
                submission.getId(),
                submission.getAssessmentId(),
                submission.getStudentId(),
                submission.getStatus(),
                submission.getStartedAt(),
                submission.getSubmittedAt(),
                answers
        );
    }

    private static AnswerResource toResource(
            Answer answer,
            GradingVisibility visibility
    ) {
        return switch (visibility) {
            case FULL -> new AnswerResource(
                    answer.getId(),
                    answer.getQuestionId(),
                    answer.getAnswerText(),
                    answer.getGradingStatus(),
                    answer.getAiGeneralFeedback(),
                    answer.getAiModel(),
                    answer.getErrorMessage(),
                    answer.getReviewedBy(),
                    answer.getCriterionResults().stream()
                            .map(SubmissionResourceFromDomainAssembler::toFullResource)
                            .toList()
            );
            case RELEASED -> new AnswerResource(
                    answer.getId(),
                    answer.getQuestionId(),
                    answer.getAnswerText(),
                    answer.getGradingStatus(),
                    answer.getAiGeneralFeedback(),
                    null,
                    null,
                    null,
                    answer.getCriterionResults().stream()
                            .map(SubmissionResourceFromDomainAssembler::toReleasedResource)
                            .toList()
            );
            case HIDDEN -> new AnswerResource(
                    answer.getId(),
                    answer.getQuestionId(),
                    answer.getAnswerText(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );
        };
    }

    private static CriterionResultResource toFullResource(
            CriterionResult criterionResult
    ) {
        return new CriterionResultResource(
                criterionResult.getId(),
                criterionResult.getRubricCriterionId(),
                criterionResult.getAiVerdict(),
                criterionResult.getAiEvidence(),
                criterionResult.getAiFeedback(),
                criterionResult.getFinalVerdict(),
                criterionResult.getFinalFeedback()
        );
    }

    private static CriterionResultResource toReleasedResource(
            CriterionResult criterionResult
    ) {
        return new CriterionResultResource(
                criterionResult.getId(),
                criterionResult.getRubricCriterionId(),
                null,
                criterionResult.getAiEvidence(),
                null,
                criterionResult.getFinalVerdict(),
                criterionResult.getFinalFeedback()
        );
    }
}
