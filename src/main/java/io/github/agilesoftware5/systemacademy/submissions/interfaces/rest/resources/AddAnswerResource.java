package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddAnswerResource(
        @NotNull UUID questionId,
        @NotBlank String answerText
) {
}
