package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.AddAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.AddAnswerResource;

import java.util.UUID;

public final class AddAnswerCommandFromResourceAssembler {

    private AddAnswerCommandFromResourceAssembler() {
    }

    public static AddAnswerCommand toCommand(
            AddAnswerResource resource,
            UUID submissionId,
            UUID studentId
    ) {
        return new AddAnswerCommand(
                submissionId,
                studentId,
                resource.questionId(),
                resource.answerText()
        );
    }
}
