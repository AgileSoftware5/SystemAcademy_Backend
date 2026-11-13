package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources;

import java.util.UUID;

/** Usuario de la sesión actual. Usa {@code userId} igual que la respuesta del login. */
public record CurrentUserResource(UUID userId, String email, String fullName, String role) {
}
