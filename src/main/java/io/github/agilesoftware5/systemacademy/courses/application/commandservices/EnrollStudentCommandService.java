package io.github.agilesoftware5.systemacademy.courses.application.commandservices;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.EnrollStudentCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface EnrollStudentCommandService {

    Result<Enrollment> handle(EnrollStudentCommand command);
}
