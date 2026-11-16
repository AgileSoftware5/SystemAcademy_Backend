package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/** Reemplazo completo de los campos editables de un borrador; instrucciones y fechas pueden ir null. */
public record UpdateAssessmentResource(@NotBlank String title, String instructions, Instant opensAt, Instant closesAt) {
}
