package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.UpdateOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.UpdateOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UpdateOpenQuestionCommandServiceImpl
        implements UpdateOpenQuestionCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(UpdateOpenQuestionCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public UpdateOpenQuestionCommandServiceImpl(
            AssessmentRepository assessmentRepository,
            CourseOwnershipGuard courseOwnershipGuard) {

        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    public Result<Assessment> handle(UpdateOpenQuestionCommand command) {

        log.debug(
                "editando pregunta assessmentId={} questionId={} teacherId={}",
                command.assessmentId(),
                command.questionId(),
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
                    "intento de editar evaluación ajena: assessmentId={} teacherId={}",
                    command.assessmentId(),
                    command.teacherId()
            );

            return Result.failure(ownership.getError());
        }

        try {
            assessment.updateOpenQuestion(
                    command.questionId(),
                    command.stem(),
                    command.modelAnswer(),
                    command.explanation(),
                    command.criteriaDescriptions()
            );

        } catch (NoSuchElementException ex) {
            log.warn("pregunta no encontrada: {}", ex.getMessage());

            return Result.failure(
                    ApplicationError.notFound(ex.getMessage())
            );

        } catch (IllegalArgumentException ex) {
            log.warn("datos de pregunta inválidos: {}", ex.getMessage());

            return Result.failure(
                    ApplicationError.validationError(ex.getMessage())
            );

        } catch (IllegalStateException ex) {
            log.warn("no se pudo editar pregunta: {}", ex.getMessage());

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }

        var saved = assessmentRepository.save(assessment);

        log.info(
                "pregunta editada id={} assessmentId={}",
                command.questionId(),
                saved.getId()
        );

        return Result.success(saved);
    }
}
