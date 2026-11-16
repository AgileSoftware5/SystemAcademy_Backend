package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import java.time.Instant;

/** Las fechas son opcionales: si faltan se usan las guardadas en el borrador (ver BE-05). */
public record PublishAssessmentResource(Instant opensAt, Instant closesAt) {
}
