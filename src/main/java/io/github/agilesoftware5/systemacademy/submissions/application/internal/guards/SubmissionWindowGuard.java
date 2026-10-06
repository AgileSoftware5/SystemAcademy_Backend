package io.github.agilesoftware5.systemacademy.submissions.application.internal.guards;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/** Un alumno solo puede iniciar, responder o entregar dentro de la ventana de la evaluación publicada. */
@Component
public class SubmissionWindowGuard {

    private final AssessmentAccessProvider assessmentAccess;
    private final Clock clock;

    public SubmissionWindowGuard(AssessmentAccessProvider assessmentAccess, Clock clock) {
        this.assessmentAccess = assessmentAccess;
        this.clock = clock;
    }

    public Result<Void> ensureOpen(UUID assessmentId) {
        if (!assessmentAccess.isOpenForSubmissions(assessmentId, clock.instant())) {
            return Result.failure(
                    ApplicationError.businessRuleViolation("la evaluación no está abierta para rendir")
            );
        }
        return Result.success(null);
    }
}
