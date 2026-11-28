package io.github.agilesoftware5.systemacademy.analytics.interfaces.rest;

import io.github.agilesoftware5.systemacademy.analytics.application.queryservices.AnalyticsQueryService;
import io.github.agilesoftware5.systemacademy.analytics.application.queryservices.AnalyticsQueryService.GetOverviewQuery;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/overview")
    @Operation(
            summary = "Métricas agregadas de actividad y calificación",
            description = "ADMIN ve toda la institución y puede filtrar por docente o curso. TEACHER ve solo sus cursos. "
                    + "Por defecto, los últimos 30 días. Las tasas con menos de 5 casos llegan en null.")
    public ResponseEntity<?> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID courseId,
            Authentication authentication
    ) {
        var result = analyticsQueryService.handle(new GetOverviewQuery(
                AuthenticatedUser.id(authentication),
                AuthenticatedUser.hasRole(authentication, "ADMIN"),
                from, to, teacherId, courseId));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.ok(result.getValue());
    }
}
