package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.util.UUID;

public record CreateAssessmentCommand(UUID courseId, UUID teacherId, String title, String instructions) {
}
