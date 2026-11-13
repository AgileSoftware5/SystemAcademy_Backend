package io.github.agilesoftware5.systemacademy.courses.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.courses.application.commandservices.CreateCourseCommandService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.commands.CreateCourseCommand;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import io.github.agilesoftware5.systemacademy.courses.domain.services.EnrollmentCodeGenerator;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateCourseCommandServiceImpl implements CreateCourseCommandService {

    private static final Logger log = LoggerFactory.getLogger(CreateCourseCommandServiceImpl.class);
    private static final int MAX_ENROLLMENT_CODE_ATTEMPTS = 5;

    private final CourseRepository courseRepository;
    private final EnrollmentCodeGenerator enrollmentCodeGenerator;

    public CreateCourseCommandServiceImpl(CourseRepository courseRepository, EnrollmentCodeGenerator enrollmentCodeGenerator) {
        this.courseRepository = courseRepository;
        this.enrollmentCodeGenerator = enrollmentCodeGenerator;
    }

    @Override
    public Result<Course> handle(CreateCourseCommand command) {
        log.debug("creando curso code={} instructorId={}", command.code(), command.instructorId());

        if (courseRepository.existsByCode(command.code())) {
            log.warn("intento de crear curso con code duplicado: {}", command.code());
            return Result.failure(ApplicationError.conflict("ya existe un curso con ese code"));
        }

        String enrollmentCode = generateUniqueEnrollmentCode();
        if (enrollmentCode == null) {
            log.error("no se pudo generar un enrollmentCode único tras {} intentos", MAX_ENROLLMENT_CODE_ATTEMPTS);
            return Result.failure(ApplicationError.unexpected("no se pudo generar un código de matrícula único"));
        }

        try {
            var course = Course.create(command.instructorId(), command.createdBy(), command.code(),
                    command.name(), command.description(), enrollmentCode);
            var saved = courseRepository.save(course);
            log.info("curso creado id={} code={} instructorId={}", saved.getId(), saved.getCode(), saved.getInstructorId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("datos de curso inválidos: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError(ex.getMessage()));
        }
    }

    private String generateUniqueEnrollmentCode() {
        for (int attempt = 0; attempt < MAX_ENROLLMENT_CODE_ATTEMPTS; attempt++) {
            String candidate = enrollmentCodeGenerator.generate();
            if (!courseRepository.existsByEnrollmentCode(candidate)) {
                return candidate;
            }
            log.debug("colisión de enrollmentCode generado, reintentando (intento {})", attempt + 1);
        }
        return null;
    }
}
