package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.util.UUID;

public record ReleaseAssessmentResultsCommand(UUID assessmentId, UUID teacherId) {
}
