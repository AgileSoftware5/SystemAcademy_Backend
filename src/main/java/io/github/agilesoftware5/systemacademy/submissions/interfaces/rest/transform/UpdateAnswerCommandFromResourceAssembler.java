package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.UpdateAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.UpdateAnswerResource;

import java.util.UUID;

public final class UpdateAnswerCommandFromResourceAssembler {

    private UpdateAnswerCommandFromResourceAssembler() {
    }

    public static UpdateAnswerCommand toCommand(
            UpdateAnswerResource resource,
            UUID submissionId,
            UUID studentId,
            UUID answerId
    ) {
        return new UpdateAnswerCommand(
                submissionId,
                studentId,
                answerId,
                resource.answerText()
        );
    }
}
