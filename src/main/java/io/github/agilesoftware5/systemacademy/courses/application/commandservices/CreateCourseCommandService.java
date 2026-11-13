package io.github.agilesoftware5.systemacademy.courses.application.commandservices;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.CreateCourseCommand;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

public interface CreateCourseCommandService {

    Result<Course> handle(CreateCourseCommand command);
}
