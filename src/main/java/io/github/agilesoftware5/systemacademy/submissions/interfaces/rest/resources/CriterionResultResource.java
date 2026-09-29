package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;

import java.util.UUID;

public record CriterionResultResource(
        UUID id,
        UUID rubricCriterionId,
        CriterionVerdict aiVerdict,
        String aiEvidence,
        String aiFeedback,
        CriterionVerdict finalVerdict,
        String finalFeedback
) {
}
