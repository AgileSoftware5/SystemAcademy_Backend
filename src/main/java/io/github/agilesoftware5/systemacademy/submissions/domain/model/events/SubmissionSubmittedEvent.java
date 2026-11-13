package io.github.agilesoftware5.systemacademy.submissions.domain.model.events;

import java.util.UUID;

public record SubmissionSubmittedEvent(UUID submissionId, UUID assessmentId) {
}
