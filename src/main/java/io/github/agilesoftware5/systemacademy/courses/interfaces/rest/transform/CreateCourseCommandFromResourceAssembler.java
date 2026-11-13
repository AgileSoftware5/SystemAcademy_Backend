package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.CreateCourseCommand;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CreateCourseResource;

import java.util.UUID;

public final class CreateCourseCommandFromResourceAssembler {

    private CreateCourseCommandFromResourceAssembler() {
    }

    public static CreateCourseCommand toCommand(CreateCourseResource resource, UUID instructorId) {
        return new CreateCourseCommand(instructorId, instructorId, resource.code(), resource.name(), resource.description());
    }
}
