package io.github.agilesoftware5.systemacademy.submissions.domain.services;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;

public interface GradingEngine {
    GradingResult grade(QuestionGradingInput input);
}