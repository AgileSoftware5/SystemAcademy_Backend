package io.github.agilesoftware5.systemacademy.assessments.application.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.PublishAssessmentCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface PublishAssessmentCommandService {

    Result<Assessment> handle(PublishAssessmentCommand command);
}
