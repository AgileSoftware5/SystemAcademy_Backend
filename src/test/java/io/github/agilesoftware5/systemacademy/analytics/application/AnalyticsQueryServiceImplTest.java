package io.github.agilesoftware5.systemacademy.analytics.application;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.AnalyticsFilter;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport;
import io.github.agilesoftware5.systemacademy.analytics.application.internal.AnalyticsQueryServiceImpl;
import io.github.agilesoftware5.systemacademy.analytics.application.queryservices.AnalyticsQueryService.GetOverviewQuery;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsQueryServiceImplTest {

    private AnalyticsFilter captured;

    private final AnalyticsQueryServiceImpl service = new AnalyticsQueryServiceImpl((filter, from, to) -> {
        captured = filter;
        return new OverviewReport(from, to, null, List.of(), List.of(), null, null, List.of());
    });

    private final UUID actor = UUID.randomUUID();
    private final LocalDate to = LocalDate.of(2026, 11, 28);

    @Test
    void adminWithoutFiltersSeesTheWholeInstitutionIncludingUsers() {
        var result = service.handle(new GetOverviewQuery(actor, true, to.minusDays(6), to, null, null));

        assertTrue(result.isSuccess());
        assertNull(captured.teacherId());
        assertTrue(captured.includeUsers());
    }

    @Test
    void adminFilteringByTeacherDoesNotIncludeInstitutionUsers() {
        var teacher = UUID.randomUUID();

        service.handle(new GetOverviewQuery(actor, true, to.minusDays(6), to, teacher, null));

        assertEquals(teacher, captured.teacherId());
        assertFalse(captured.includeUsers());
    }

    @Test
    void teacherIsAlwaysScopedToTheirOwnCourses() {
        service.handle(new GetOverviewQuery(actor, false, to.minusDays(6), to, null, null));

        assertEquals(actor, captured.teacherId());
        assertFalse(captured.includeUsers());
    }

    @Test
    void teacherCannotAskForAnotherTeacher() {
        var result = service.handle(new GetOverviewQuery(actor, false, to.minusDays(6), to, UUID.randomUUID(), null));

        assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
    }

    @Test
    void rangeEndsTheNextMidnightSoTheLastDayIsIncluded() {
        service.handle(new GetOverviewQuery(actor, true, to, to, null, null));

        assertEquals(captured.from().plusSeconds(86_400), captured.toExclusive());
    }

    @Test
    void defaultsToTheLastThirtyDays() {
        var result = service.handle(new GetOverviewQuery(actor, true, null, null, null, null));

        assertEquals(29, java.time.temporal.ChronoUnit.DAYS.between(result.getValue().from(), result.getValue().to()));
    }

    @Test
    void rejectsAReversedOrTooWideRange() {
        var reversed = service.handle(new GetOverviewQuery(actor, true, to, to.minusDays(1), null, null));
        var wide = service.handle(new GetOverviewQuery(actor, true, to.minusDays(400), to, null, null));

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, reversed.getError().code());
        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, wide.getError().code());
    }
}
