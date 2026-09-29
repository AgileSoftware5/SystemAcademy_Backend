package io.github.agilesoftware5.systemacademy.submissions.domain.model.commands;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;

import java.util.UUID;

public record OverrideCriterionResultCommand(
        UUID submissionId,
        UUID answerId,
        UUID criterionResultId,
        CriterionVerdict finalVerdict,
        String finalFeedback
) {}
