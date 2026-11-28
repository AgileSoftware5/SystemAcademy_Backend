package io.github.agilesoftware5.systemacademy.analytics.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AnalyticsFilter(Instant from, Instant toExclusive, UUID teacherId, UUID courseId, boolean includeUsers) {
}
