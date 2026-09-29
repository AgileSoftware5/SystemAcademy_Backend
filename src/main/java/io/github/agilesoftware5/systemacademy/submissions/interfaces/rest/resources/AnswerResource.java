package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;

import java.util.List;
import java.util.UUID;

public record AnswerResource(
        UUID id,
        UUID questionId,
        String answerText,
        GradingStatus gradingStatus,
        String aiGeneralFeedback,
        String aiModel,
        String errorMessage,
        UUID reviewedBy,
        List<CriterionResultResource> criterionResults
) {
}
