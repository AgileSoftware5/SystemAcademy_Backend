package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

public record QuestionResource(
        UUID id,
        int position,
        String stem,
        String modelAnswer,
        String explanation,
        List<RubricCriterionResource> criteria
) {
}
