package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ChangeUserStatusResource(@NotBlank String status) {
}
