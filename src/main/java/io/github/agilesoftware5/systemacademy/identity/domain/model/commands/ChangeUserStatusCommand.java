package io.github.agilesoftware5.systemacademy.identity.domain.model.commands;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;

import java.util.UUID;

public record ChangeUserStatusCommand(UUID actorId, UUID targetId, UserStatus status) {
}
