package io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects;

import java.util.UUID;

public record RubricCriterionInput(
        UUID rubricCriterionId,
        String description
) {
}
