package io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects;

import java.util.UUID;

/** Resultado de un login exitoso: token emitido + datos mínimos del usuario. */
public record AuthenticatedSession(String token, UUID userId, Role role) {
}
