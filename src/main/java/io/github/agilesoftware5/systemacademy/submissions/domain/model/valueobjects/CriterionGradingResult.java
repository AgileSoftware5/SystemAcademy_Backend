package io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects;

import java.util.UUID;

public record CriterionGradingResult(
        UUID rubricCriterionId,
        CriterionVerdict verdict,
        String evidence,
        String feedback
) {
}
