package io.github.agilesoftware5.systemacademy.assessments.domain.model.commands;

import java.util.List;
import java.util.UUID;

public record AddOpenQuestionCommand(
        UUID assessmentId,
        UUID teacherId,
        String stem,
        String modelAnswer,
        String explanation,
        List<String> criteriaDescriptions
) {
}
