package io.github.agilesoftware5.systemacademy.submissions.domain.model.commands;

import java.util.UUID;

public record StartSubmissionCommand(
        UUID assessmentId,
        UUID studentId
) {
}
