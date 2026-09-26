package io.github.agilesoftware5.systemacademy.identity.domain.model.commands;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;

public record RegisterUserCommand(String email, String rawPassword, String fullName, Role role) {
}
