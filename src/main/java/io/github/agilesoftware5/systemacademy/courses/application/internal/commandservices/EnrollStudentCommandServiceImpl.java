package io.github.agilesoftware5.systemacademy.courses.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.courses.application.commandservices.EnrollStudentCommandService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.EnrollStudentCommand;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnrollStudentCommandServiceImpl implements EnrollStudentCommandService {

    private static final Logger log = LoggerFactory.getLogger(EnrollStudentCommandServiceImpl.class);

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollStudentCommandServiceImpl(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Result<Enrollment> handle(EnrollStudentCommand command) {
        log.debug("intento de matrícula enrollmentCode={} studentId={}", command.enrollmentCode(), command.studentId());

        var course = courseRepository.findByEnrollmentCode(command.enrollmentCode()).orElse(null);
        if (course == null || !course.isActive()) {
            log.warn("código de matrícula inválido o curso inactivo: {}", command.enrollmentCode());
            return Result.failure(ApplicationError.notFound("código de matrícula inválido"));
        }

        if (enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), command.studentId())) {
            log.warn("matrícula duplicada studentId={} courseId={}", command.studentId(), course.getId());
            return Result.failure(ApplicationError.conflict("ya estás matriculado en este curso"));
        }

        var enrollment = Enrollment.enroll(course.getId(), command.studentId());
        var saved = enrollmentRepository.save(enrollment);
        log.info("alumno matriculado studentId={} courseId={}", saved.getStudentId(), saved.getCourseId());
        return Result.success(saved);
    }
}
