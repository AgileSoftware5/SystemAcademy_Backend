package io.github.agilesoftware5.systemacademy.shared.application.results;

import java.time.Instant;

public record AuditedResult<T>(
        T data,
        Instant createdAt,
        Instant updatedAt
) {}
