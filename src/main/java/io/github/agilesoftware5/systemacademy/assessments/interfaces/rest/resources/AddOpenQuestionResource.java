package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddOpenQuestionResource(
        @NotBlank String stem,
        @NotBlank String modelAnswer,
        String explanation,
        @NotEmpty List<@NotBlank String> criteriaDescriptions
) {
}
