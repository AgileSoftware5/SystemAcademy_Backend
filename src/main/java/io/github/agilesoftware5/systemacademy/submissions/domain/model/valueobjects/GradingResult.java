package io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects;

import java.util.List;

public record GradingResult(
        String generalFeedback,
        String model,
        List<CriterionGradingResult> criteria
) {
}
