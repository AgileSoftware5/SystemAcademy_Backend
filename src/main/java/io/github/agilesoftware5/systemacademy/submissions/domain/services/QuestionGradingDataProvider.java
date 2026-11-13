package io.github.agilesoftware5.systemacademy.submissions.domain.services;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;

import java.util.UUID;

public interface QuestionGradingDataProvider {

    QuestionGradingInput getQuestionGradingInput(
            UUID assessmentId,
            UUID questionId,
            String answerText
    );
}
