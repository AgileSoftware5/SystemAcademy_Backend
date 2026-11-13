package io.github.agilesoftware5.systemacademy.courses.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("coursesCourseOwnershipGuard")
public class CourseOwnershipGuard {

    private final CourseQueryService courseQueryService;

    public CourseOwnershipGuard(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    public Result<Void> ensureOwner(UUID courseId, UUID teacherId) {

        var course = courseQueryService.handle(
                new GetCourseByIdQuery(courseId)
        ).orElse(null);

        if (course == null) {
            return Result.failure(
                    ApplicationError.notFound("curso no encontrado")
            );
        }

        if (!course.getInstructorId().equals(teacherId)) {
            return Result.failure(
                    ApplicationError.forbidden(
                            "no eres el docente propietario de este curso"
                    )
            );
        }

        return Result.success(null);
    }
}
