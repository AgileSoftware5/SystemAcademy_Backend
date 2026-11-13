package io.github.agilesoftware5.systemacademy.assessments.application.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.CreateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface CreateAssessmentCommandService {

    Result<Assessment> handle(CreateAssessmentCommand command);
}
