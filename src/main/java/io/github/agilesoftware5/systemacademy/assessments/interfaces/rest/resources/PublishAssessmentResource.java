package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PublishAssessmentResource(@NotNull Instant opensAt, @NotNull Instant closesAt) {
}
