package io.github.agilesoftware5.systemacademy.submissions.application.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.AddAnswerCommand;

public interface AddAnswerCommandService {

    Result<Submission> handle(AddAnswerCommand command);
}