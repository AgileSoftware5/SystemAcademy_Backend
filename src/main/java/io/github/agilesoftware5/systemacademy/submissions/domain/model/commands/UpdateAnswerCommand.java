package io.github.agilesoftware5.systemacademy.submissions.domain.model.commands;

import java.util.UUID;

public record UpdateAnswerCommand(
        UUID submissionId,
        UUID studentId,
        UUID answerId,
        String answerText
) {
}
