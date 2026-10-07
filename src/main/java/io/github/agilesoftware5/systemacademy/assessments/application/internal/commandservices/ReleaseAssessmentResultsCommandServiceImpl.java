package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.ReleaseAssessmentResultsCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.internal.outboundservices.GradingProgressService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.ReleaseAssessmentResultsCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ReleaseAssessmentResultsCommandServiceImpl implements ReleaseAssessmentResultsCommandService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseAssessmentResultsCommandServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final CourseOwnershipGuard courseOwnershipGuard;
    private final GradingProgressService gradingProgressService;

    public ReleaseAssessmentResultsCommandServiceImpl(AssessmentRepository assessmentRepository,
                                                      CourseOwnershipGuard courseOwnershipGuard,
                                                      GradingProgressService gradingProgressService) {
        this.assessmentRepository = assessmentRepository;
        this.courseOwnershipGuard = courseOwnershipGuard;
        this.gradingProgressService = gradingProgressService;
    }

    @Override
    public Result<Assessment> handle(ReleaseAssessmentResultsCommand command) {
        log.debug("publicando resultados assessmentId={} teacherId={}", command.assessmentId(), command.teacherId());

        var assessment = assessmentRepository.findById(command.assessmentId()).orElse(null);
        if (assessment == null) {
            log.warn("evaluación no encontrada id={}", command.assessmentId());
            return Result.failure(ApplicationError.notFound("evaluación no encontrada"));
        }

        var ownership = courseOwnershipGuard.ensureOwner(assessment.getCourseId(), command.teacherId());
        if (ownership.isFailure()) {
            log.warn("intento de publicar resultados ajenos assessmentId={} teacherId={}",
                    command.assessmentId(), command.teacherId());
            return Result.failure(ownership.getError());
        }

        long ungraded = gradingProgressService.countUngradedAnswers(assessment.getId());
        if (ungraded > 0) {
            log.warn("resultados no publicados assessmentId={} respuestasSinCalificar={}", assessment.getId(), ungraded);
            return Result.failure(ApplicationError.businessRuleViolation(
                    "quedan " + ungraded + " respuestas sin calificar"));
        }

        try {
            assessment.releaseResults(Instant.now());
            var saved = assessmentRepository.save(assessment);
            log.info("resultados publicados assessmentId={} releasedAt={}", saved.getId(), saved.getResultsReleasedAt());
            return Result.success(saved);
        } catch (IllegalStateException ex) {
            log.warn("no se pudieron publicar resultados assessmentId={}: {}", assessment.getId(), ex.getMessage());
            return Result.failure(ApplicationError.businessRuleViolation(ex.getMessage()));
        }
    }
}
