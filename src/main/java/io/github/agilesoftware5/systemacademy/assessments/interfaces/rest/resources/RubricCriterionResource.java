package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import java.util.UUID;

public record RubricCriterionResource(UUID id, int position, String description) {
}
