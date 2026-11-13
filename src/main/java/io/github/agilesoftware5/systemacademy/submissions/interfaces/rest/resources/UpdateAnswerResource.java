package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdateAnswerResource(
        @NotBlank String answerText
) {
}
