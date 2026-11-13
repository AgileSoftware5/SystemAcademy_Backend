package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.OverrideCriterionResultCommand;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.OverrideCriterionResultResource;

import java.util.UUID;

public final class OverrideCriterionResultCommandFromResourceAssembler {

    private OverrideCriterionResultCommandFromResourceAssembler() {
    }

    public static OverrideCriterionResultCommand toCommand(
            OverrideCriterionResultResource resource,
            UUID submissionId,
            UUID answerId,
            UUID criterionResultId,
            UUID reviewerId
    ) {
        return new OverrideCriterionResultCommand(
                submissionId,
                answerId,
                criterionResultId,
                resource.finalVerdict(),
                resource.finalFeedback(),
                reviewerId
        );
    }
}
