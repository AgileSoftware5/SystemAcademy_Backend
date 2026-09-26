package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources;

import java.util.UUID;

public record AuthenticatedSessionResource(String token, UUID userId, String role) {
}
