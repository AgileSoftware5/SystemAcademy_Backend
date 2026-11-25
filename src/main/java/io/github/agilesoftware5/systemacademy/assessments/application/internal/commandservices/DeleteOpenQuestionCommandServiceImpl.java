package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.DeleteOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.DeleteOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class DeleteOpenQuestionCommandServiceImpl implements DeleteOpenQuestionCommandService {

    private static final Logger log = LoggerFactory.getLogger(DeleteOpenQuestionCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;

    public DeleteOpenQuestionCommandServiceImpl(
            AssessmentRepository assessmentRepository,
            CourseOwnershipGuard courseOwnershipGuard) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
    }

    @Override
    public Result<Assessment> handle(DeleteOpenQuestionCommand command) {

        log.debug(
                "eliminando pregunta assessmentId={} questionId={} teacherId={}",
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
            assessment.deleteOpenQuestion(command.questionId());

        } catch (NoSuchElementException ex) {
            log.warn("pregunta no encontrada: {}", ex.getMessage());

            return Result.failure(
                    ApplicationError.notFound(ex.getMessage())
            );

        } catch (IllegalStateException ex) {
            log.warn(
                    "no se pudo eliminar pregunta: {}",
                    ex.getMessage()
            );

            return Result.failure(
                    ApplicationError.businessRuleViolation(ex.getMessage())
            );
        }

        var saved = assessmentRepository.save(assessment);

        log.info(
                "pregunta eliminada id={} assessmentId={}",
                command.questionId(),
                saved.getId()
        );

        return Result.success(saved);
    }
}
