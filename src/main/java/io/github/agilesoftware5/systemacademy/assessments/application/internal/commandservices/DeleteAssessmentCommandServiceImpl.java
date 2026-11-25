package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.DeleteAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.DeleteAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAssessmentCommandServiceImpl
        implements DeleteAssessmentCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(DeleteAssessmentCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public DeleteAssessmentCommandServiceImpl(
            AssessmentRepository assessmentRepository,
            CourseOwnershipGuard courseOwnershipGuard) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    @Transactional
    public Result<Void> handle(DeleteAssessmentCommand command) {

        log.debug(
                "eliminando evaluación assessmentId={} teacherId={}",
                command.assessmentId(),
                command.teacherId()
        );

        var assessment = assessmentRepository
                .findById(command.assessmentId())
                .orElse(null);

        if (assessment == null) {
            log.warn(
                    "evaluación no encontrada id={}",
                    command.assessmentId()
            );

            return Result.failure(
                    ApplicationError.notFound("evaluación no encontrada")
            );
        }

        var ownership = courseOwnershipGuard.ensureOwner(
                assessment.getCourseId(),
                command.teacherId()
        );

        if (ownership.isFailure()) {
            log.warn(
                    "intento de eliminar evaluación ajena: assessmentId={} teacherId={}",
                    command.assessmentId(),
                    command.teacherId()
            );

            return Result.failure(ownership.getError());
        }

        try {
            assessment.ensureDeletable();
        } catch (IllegalStateException ex) {
            log.warn(
                    "no se pudo eliminar evaluación: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }

        assessmentRepository.delete(assessment);

        log.info(
                "eliminación de evaluación solicitada id={}",
                assessment.getId()
        );

        return Result.success(null);
    }
}