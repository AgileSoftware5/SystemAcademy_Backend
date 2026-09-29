package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SubmissionResource(
        UUID id,
        UUID assessmentId,
        UUID studentId,
        SubmissionStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        List<AnswerResource> answers
) {
}
