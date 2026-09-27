package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida que un docente sea dueño del curso antes de dejarlo crear/editar evaluaciones en él.
 * Llama directo al application service de courses (mismo monolito) — no hace falta ACL
 * para una consulta simple dentro del mismo proceso.
 */
@Component
public class CourseOwnershipGuard {

    private final CourseQueryService courseQueryService;

    public CourseOwnershipGuard(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    public Result<Void> ensureOwner(UUID courseId, UUID teacherId) {
        var course = courseQueryService.handle(new GetCourseByIdQuery(courseId)).orElse(null);
        if (course == null) {
            return Result.failure(ApplicationError.notFound("curso no encontrado"));
        }
        if (!course.getInstructorId().equals(teacherId)) {
            return Result.failure(ApplicationError.businessRuleViolation("no sos el docente dueño de este curso"));
        }
        return Result.success(null);
    }
}
