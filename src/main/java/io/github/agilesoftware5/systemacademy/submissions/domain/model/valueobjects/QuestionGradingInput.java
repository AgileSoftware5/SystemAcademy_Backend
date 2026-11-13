package io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects;

import java.util.List;
import java.util.UUID;

public record QuestionGradingInput(
        UUID questionId,
        String questionStem,
        String modelAnswer,
        String answerText,
        List<RubricCriterionInput> criteria
) {
}
