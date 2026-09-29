package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.AnswerResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.CriterionResultResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.SubmissionResource;

public final class SubmissionResourceFromDomainAssembler {

    private SubmissionResourceFromDomainAssembler() {
    }

    public static SubmissionResource toResource(
            Submission submission
    ) {
        var answers = submission.getAnswers()
                .stream()
                .map(SubmissionResourceFromDomainAssembler::toResource)
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
            Answer answer
    ) {
        var criterionResults = answer.getCriterionResults()
                .stream()
                .map(SubmissionResourceFromDomainAssembler::toResource)
                .toList();

        return new AnswerResource(
                answer.getId(),
                answer.getQuestionId(),
                answer.getAnswerText(),
                answer.getGradingStatus(),
                answer.getAiGeneralFeedback(),
                answer.getAiModel(),
                answer.getErrorMessage(),
                answer.getReviewedBy(),
                criterionResults
        );
    }

    private static CriterionResultResource toResource(
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
}
