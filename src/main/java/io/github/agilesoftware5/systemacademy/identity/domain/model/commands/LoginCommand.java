package io.github.agilesoftware5.systemacademy.identity.domain.model.commands;

public record LoginCommand(String email, String rawPassword) {
}
