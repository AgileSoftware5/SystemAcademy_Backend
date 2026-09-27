package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.AddOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AddOpenQuestionResource;

import java.util.UUID;

public final class AddOpenQuestionCommandFromResourceAssembler {

    private AddOpenQuestionCommandFromResourceAssembler() {
    }

    public static AddOpenQuestionCommand toCommand(AddOpenQuestionResource resource, UUID assessmentId, UUID teacherId) {
        return new AddOpenQuestionCommand(assessmentId, teacherId, resource.stem(), resource.modelAnswer(),
                resource.explanation(), resource.criteriaDescriptions());
    }
}
