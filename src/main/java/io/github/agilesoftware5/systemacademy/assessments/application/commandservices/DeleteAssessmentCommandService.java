package io.github.agilesoftware5.systemacademy.assessments.application.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.DeleteAssessmentCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface DeleteAssessmentCommandService {

    Result<Void> handle(DeleteAssessmentCommand command);
}
