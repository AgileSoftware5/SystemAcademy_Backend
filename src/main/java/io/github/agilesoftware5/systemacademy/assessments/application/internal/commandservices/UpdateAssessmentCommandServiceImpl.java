package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.UpdateAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.UpdateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateAssessmentCommandServiceImpl implements UpdateAssessmentCommandService {

    private static final Logger log = LoggerFactory.getLogger(UpdateAssessmentCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public UpdateAssessmentCommandServiceImpl(AssessmentRepository assessmentRepository, CourseOwnershipGuard courseOwnershipGuard) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    public Result<Assessment> handle(UpdateAssessmentCommand command) {
        log.debug("editando borrador id={} teacherId={}", command.assessmentId(), command.teacherId());

        var assessment = assessmentRepository.findById(command.assessmentId()).orElse(null);
        if (assessment == null) {
            log.warn("evaluación no encontrada id={}", command.assessmentId());
            return Result.failure(ApplicationError.notFound("evaluación no encontrada"));
        }

        var ownership = courseOwnershipGuard.ensureOwner(assessment.getCourseId(), command.teacherId());
        if (ownership.isFailure()) {
            log.warn("intento de editar evaluación ajena: assessmentId={} teacherId={}", command.assessmentId(), command.teacherId());
            return Result.failure(ownership.getError());
        }

        try {
            assessment.updateDraft(command.title(), command.instructions(), command.opensAt(), command.closesAt());
            var saved = assessmentRepository.save(assessment);
            log.info("borrador editado id={}", saved.getId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("datos de edición inválidos: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError(ex.getMessage()));
        } catch (IllegalStateException ex) {
            log.warn("no se pudo editar: {}", ex.getMessage());
            return Result.failure(ApplicationError.businessRuleViolation(ex.getMessage()));
        }
    }
}
