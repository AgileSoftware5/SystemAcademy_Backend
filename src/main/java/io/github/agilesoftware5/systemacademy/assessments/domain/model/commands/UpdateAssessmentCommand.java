package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record UpdateAssessmentCommand(UUID assessmentId, UUID teacherId, String title, String instructions,
                                      Instant opensAt, Instant closesAt) {
}
