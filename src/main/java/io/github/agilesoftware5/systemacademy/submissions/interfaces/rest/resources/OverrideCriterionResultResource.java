package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import jakarta.validation.constraints.NotNull;

public record OverrideCriterionResultResource(
        @NotNull CriterionVerdict finalVerdict,
        String finalFeedback
) {}
