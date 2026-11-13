package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.CreateAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.CreateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateAssessmentCommandServiceImpl implements CreateAssessmentCommandService {

    private static final Logger log = LoggerFactory.getLogger(CreateAssessmentCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public CreateAssessmentCommandServiceImpl(AssessmentRepository assessmentRepository, CourseOwnershipGuard courseOwnershipGuard) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    public Result<Assessment> handle(CreateAssessmentCommand command) {
        log.debug("creando evaluación courseId={} teacherId={}", command.courseId(), command.teacherId());

        var ownership = courseOwnershipGuard.ensureOwner(command.courseId(), command.teacherId());
        if (ownership.isFailure()) {
            log.warn("intento de crear evaluación sin ser dueño del curso: courseId={} teacherId={}", command.courseId(), command.teacherId());
            return Result.failure(ownership.getError());
        }

        try {
            var assessment = Assessment.create(command.courseId(), command.title(), command.instructions());
            var saved = assessmentRepository.save(assessment);
            log.info("evaluación creada id={} courseId={}", saved.getId(), saved.getCourseId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("datos de evaluación inválidos: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError(ex.getMessage()));
        }
    }
}
