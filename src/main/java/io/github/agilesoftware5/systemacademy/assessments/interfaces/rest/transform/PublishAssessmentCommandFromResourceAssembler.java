package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.PublishAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.PublishAssessmentResource;

import java.util.UUID;

public final class PublishAssessmentCommandFromResourceAssembler {

    private PublishAssessmentCommandFromResourceAssembler() {
    }

    public static PublishAssessmentCommand toCommand(PublishAssessmentResource resource, UUID assessmentId, UUID teacherId) {
        return new PublishAssessmentCommand(assessmentId, teacherId, resource.opensAt(), resource.closesAt());
    }
}
