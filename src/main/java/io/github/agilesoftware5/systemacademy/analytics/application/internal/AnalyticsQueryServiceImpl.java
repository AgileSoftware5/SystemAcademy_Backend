package io.github.agilesoftware5.systemacademy.analytics.application.internal;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.AnalyticsFilter;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport;
import io.github.agilesoftware5.systemacademy.analytics.application.ports.AnalyticsReadModel;
import io.github.agilesoftware5.systemacademy.analytics.application.queryservices.AnalyticsQueryService;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    static final int MAX_RANGE_DAYS = 366;

    private final AnalyticsReadModel readModel;

    public AnalyticsQueryServiceImpl(AnalyticsReadModel readModel) {
        this.readModel = readModel;
    }

    @Override
    public Result<OverviewReport> handle(GetOverviewQuery query) {
        var to = query.to() != null ? query.to() : LocalDate.now(ZoneOffset.UTC);
        var from = query.from() != null ? query.from() : to.minusDays(29);
        if (from.isAfter(to)) {
            return Result.failure(ApplicationError.validationError("from no puede ser posterior a to"));
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            return Result.failure(ApplicationError.validationError("el rango máximo es de " + MAX_RANGE_DAYS + " días"));
        }
        if (!query.admin() && query.teacherId() != null && !query.teacherId().equals(query.actorId())) {
            return Result.failure(ApplicationError.forbidden("solo puedes ver métricas de tus propios cursos"));
        }

        var teacherId = query.admin() ? query.teacherId() : query.actorId();
        var filter = new AnalyticsFilter(
                from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                teacherId,
                query.courseId(),
                query.admin() && teacherId == null && query.courseId() == null);
        return Result.success(readModel.overview(filter, from, to));
    }
}
