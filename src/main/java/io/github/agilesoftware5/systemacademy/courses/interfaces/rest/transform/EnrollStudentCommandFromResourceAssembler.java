package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.EnrollStudentCommand;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.EnrollStudentResource;

import java.util.UUID;

public final class EnrollStudentCommandFromResourceAssembler {

    private EnrollStudentCommandFromResourceAssembler() {
    }

    public static EnrollStudentCommand toCommand(EnrollStudentResource resource, UUID studentId) {
        return new EnrollStudentCommand(resource.enrollmentCode(), studentId);
    }
}
