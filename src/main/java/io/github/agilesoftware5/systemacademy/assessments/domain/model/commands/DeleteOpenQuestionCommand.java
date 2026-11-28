package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.util.UUID;

public record DeleteOpenQuestionCommand(
        UUID assessmentId,
        UUID questionId,
        UUID teacherId
) {
}
