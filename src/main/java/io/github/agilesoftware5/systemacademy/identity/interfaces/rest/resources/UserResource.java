package io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources;

import java.util.UUID;

public record UserResource(UUID id, String email, String fullName, String role, String status) {
}
