package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.UpdateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.UpdateAssessmentResource;

import java.util.UUID;

public final class UpdateAssessmentCommandFromResourceAssembler {

    private UpdateAssessmentCommandFromResourceAssembler() {
    }

    public static UpdateAssessmentCommand toCommand(UpdateAssessmentResource resource, UUID assessmentId, UUID teacherId) {
        return new UpdateAssessmentCommand(assessmentId, teacherId, resource.title(), resource.instructions(),
                resource.opensAt(), resource.closesAt());
    }
}
