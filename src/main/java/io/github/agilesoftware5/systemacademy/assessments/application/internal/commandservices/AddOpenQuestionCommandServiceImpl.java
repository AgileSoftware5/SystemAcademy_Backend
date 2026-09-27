package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.AddOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.AddOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AddOpenQuestionCommandServiceImpl implements AddOpenQuestionCommandService {

    private static final Logger log = LoggerFactory.getLogger(AddOpenQuestionCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public AddOpenQuestionCommandServiceImpl(AssessmentRepository assessmentRepository, CourseOwnershipGuard courseOwnershipGuard) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    public Result<Assessment> handle(AddOpenQuestionCommand command) {
        log.debug("agregando pregunta assessmentId={} teacherId={}", command.assessmentId(), command.teacherId());

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
            var questionId = assessment.addOpenQuestion(command.stem(), command.modelAnswer(), command.explanation(), command.criteriaDescriptions());
            var saved = assessmentRepository.save(assessment);
            log.info("pregunta agregada id={} assessmentId={}", questionId, saved.getId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("datos de pregunta inválidos: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError(ex.getMessage()));
        } catch (IllegalStateException ex) {
            log.warn("no se pudo agregar pregunta: {}", ex.getMessage());
            return Result.failure(ApplicationError.businessRuleViolation(ex.getMessage()));
        }
    }
}
