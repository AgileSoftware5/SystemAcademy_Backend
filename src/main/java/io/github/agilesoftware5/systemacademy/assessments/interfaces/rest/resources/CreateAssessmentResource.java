package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CreateAssessmentResource(@NotBlank String title, String instructions) {
}
