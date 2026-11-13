package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginResource(@Email @NotBlank String email, @NotBlank String password) {
}
