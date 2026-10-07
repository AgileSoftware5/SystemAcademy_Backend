package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.GradeAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.grading.AnswerGrader;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.GradeAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Disparo manual del docente: típicamente para reintentar una respuesta que quedó FAILED. */
@Service
public class GradeAnswerCommandServiceImpl
        implements GradeAnswerCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(GradeAnswerCommandServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final AssessmentAccessProvider assessmentAccess;
    private final AnswerGrader answerGrader;

    public GradeAnswerCommandServiceImpl(
            SubmissionRepository submissionRepository,
            AssessmentAccessProvider assessmentAccess,
            AnswerGrader answerGrader
    ) {
        this.submissionRepository = submissionRepository;
        this.assessmentAccess = assessmentAccess;
        this.answerGrader = answerGrader;
    }

    @Override
    public Result<Submission> handle(GradeAnswerCommand command) {

        var submission = submissionRepository
                .findById(command.submissionId())
                .orElse(null);

        if (submission == null) {
            log.warn("entrega no encontrada submissionId={}", command.submissionId());
            return Result.failure(
                    ApplicationError.notFound("entrega no encontrada")
            );
        }

        if (!assessmentAccess.isTaughtBy(submission.getAssessmentId(), command.teacherId())) {
            log.warn("intento de calificar entrega ajena submissionId={} teacherId={}",
                    submission.getId(), command.teacherId());
            return Result.failure(
                    ApplicationError.forbidden("no sos el docente dueño de esta evaluación")
            );
        }

        return answerGrader.grade(submission, command.answerId());
    }
}
