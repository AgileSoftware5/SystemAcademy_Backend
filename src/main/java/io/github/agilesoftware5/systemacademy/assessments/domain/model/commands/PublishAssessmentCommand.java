package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record PublishAssessmentCommand(UUID assessmentId, UUID teacherId, Instant opensAt, Instant closesAt) {
}
