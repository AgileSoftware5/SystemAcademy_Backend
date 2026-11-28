package io.github.agilesoftware5.systemacademy.analytics.application.queryservices;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

import java.time.LocalDate;
import java.util.UUID;

public interface AnalyticsQueryService {

    record GetOverviewQuery(UUID actorId, boolean admin, LocalDate from, LocalDate to, UUID teacherId, UUID courseId) {
    }

    Result<OverviewReport> handle(GetOverviewQuery query);
}
