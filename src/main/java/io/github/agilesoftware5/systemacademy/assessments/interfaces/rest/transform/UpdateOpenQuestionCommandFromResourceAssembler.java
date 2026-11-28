package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.UpdateOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.UpdateOpenQuestionResource;

import java.util.UUID;

public final class UpdateOpenQuestionCommandFromResourceAssembler {

    private UpdateOpenQuestionCommandFromResourceAssembler() {
    }

    public static UpdateOpenQuestionCommand toCommand(
            UpdateOpenQuestionResource resource,
            UUID assessmentId,
            UUID questionId,
            UUID teacherId) {

        return new UpdateOpenQuestionCommand(
                assessmentId,
                questionId,
                teacherId,
                resource.stem(),
                resource.modelAnswer(),
                resource.explanation(),
                resource.criteriaDescriptions()
        );
    }
}
