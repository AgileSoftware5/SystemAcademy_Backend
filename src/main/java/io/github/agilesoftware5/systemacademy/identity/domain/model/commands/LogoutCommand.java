package io.github.agilesoftware5.systemacademy.identity.domain.model.commands;

import java.time.Instant;

public record LogoutCommand(String tokenId, Instant tokenExpiresAt) {
}
