package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.EnrollStudentCommand;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.EnrollStudentResource;

import java.util.UUID;

public final class EnrollStudentCommandFromResourceAssembler {

    private EnrollStudentCommandFromResourceAssembler() {
    }

    /** Los códigos se generan en mayúsculas; se tolera minúsculas y espacios al teclearlos o copiarlos. */
    private static String normalize(String code) {
        return code.trim().toUpperCase();
    }

    public static EnrollStudentCommand toCommand(EnrollStudentResource resource, UUID studentId) {
        return new EnrollStudentCommand(normalize(resource.enrollmentCode()), studentId);
    }
}
