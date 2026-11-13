package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.CreateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.CreateAssessmentResource;

import java.util.UUID;

public final class CreateAssessmentCommandFromResourceAssembler {

    private CreateAssessmentCommandFromResourceAssembler() {
    }

    public static CreateAssessmentCommand toCommand(CreateAssessmentResource resource, UUID courseId, UUID teacherId) {
        return new CreateAssessmentCommand(courseId, teacherId, resource.title(), resource.instructions());
    }
}
