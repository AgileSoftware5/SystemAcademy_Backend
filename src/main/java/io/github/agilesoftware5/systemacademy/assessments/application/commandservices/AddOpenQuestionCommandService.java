package io.github.agilesoftware5.systemacademy.assessments.application.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.AddOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface AddOpenQuestionCommandService {

    Result<Assessment> handle(AddOpenQuestionCommand command);
}
