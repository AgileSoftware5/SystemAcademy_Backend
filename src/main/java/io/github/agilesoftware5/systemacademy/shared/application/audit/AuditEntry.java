package io.github.agilesoftware5.systemacademy.shared.application.audit;

import java.util.UUID;

public record AuditEntry(UUID actorId, String action, String targetType, UUID targetId, String detail) {
}
