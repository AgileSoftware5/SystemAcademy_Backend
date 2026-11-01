package io.github.agilesoftware5.systemacademy.courses.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.dto.CourseStudent;
import io.github.agilesoftware5.systemacademy.courses.application.ports.outbound.StudentIdentityQuery;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.ListStudentsByCourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListStudentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListStudentsByCourseQueryServiceImpl
        implements ListStudentsByCourseQueryService {

    private final CourseOwnershipGuard courseOwnershipGuard;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentIdentityQuery studentIdentityQuery;

    public ListStudentsByCourseQueryServiceImpl(
            CourseOwnershipGuard courseOwnershipGuard,
            EnrollmentRepository enrollmentRepository,
            StudentIdentityQuery studentIdentityQuery
    ) {
        this.courseOwnershipGuard = courseOwnershipGuard;
        this.enrollmentRepository = enrollmentRepository;
        this.studentIdentityQuery = studentIdentityQuery;
    }

    @Override
    public Result<List<CourseStudent>> handle(ListStudentsByCourseQuery query) {

        // 1. Verificar que el docente sea propietario del curso.
        var ownershipResult = courseOwnershipGuard.ensureOwner(
                query.courseId(),
                query.teacherId()
        );

        if (ownershipResult.isFailure()) {
            return Result.failure(ownershipResult.getError());
        }

        // 2. Obtener las matrículas activas del curso.
        List<Enrollment> enrollments =
                enrollmentRepository.findByCourseIdAndStatus(
                        query.courseId(),
                        EnrollmentStatus.ACTIVE
                );

        if (enrollments.isEmpty()) {
            return Result.success(List.of());
        }

        // 3. Obtener los identificadores de los estudiantes.
        List<UUID> studentIds = enrollments.stream()
                .map(Enrollment::getStudentId)
                .distinct()
                .toList();

        // 4. Consultar los perfiles mediante el puerto de salida.
        var profiles = studentIdentityQuery.findStudentProfilesByIds(studentIds);

        // 5. Indexar los perfiles por studentId para encontrarlos fácilmente.
        Map<UUID, io.github.agilesoftware5.systemacademy.courses.application.dto.StudentProfile>
                profilesById = profiles.stream()
                .collect(Collectors.toMap(
                        profile -> profile.studentId(),
                        Function.identity()
                ));

        // 6. Combinar los perfiles con la fecha de matrícula.
        List<CourseStudent> students = enrollments.stream()
                .filter(enrollment ->
                        profilesById.containsKey(enrollment.getStudentId())
                )
                .map(enrollment -> {
                    var profile = profilesById.get(enrollment.getStudentId());

                    return new CourseStudent(
                            profile.studentId(),
                            profile.fullName(),
                            profile.email(),
                            enrollment.getEnrolledAt()
                    );
                })
                .toList();

        return Result.success(students);
    }
}
