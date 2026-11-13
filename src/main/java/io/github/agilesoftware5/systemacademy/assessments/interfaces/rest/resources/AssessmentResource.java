package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssessmentResource(
        UUID id,
        UUID courseId,
        String title,
        String instructions,
        Instant opensAt,
        Instant closesAt,
        String status,
        Instant resultsReleasedAt,
        List<QuestionResource> questions
) {
}
