package io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects;

import java.time.Instant;

/** Identidad del token con el que se hizo la petición actual: su id (jti) y cuándo expira. */
public record AuthenticatedToken(String id, Instant expiresAt) {
}
