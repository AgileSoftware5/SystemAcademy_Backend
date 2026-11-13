package io.github.agilesoftware5.systemacademy.submissions.application.internal.guards;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubmissionOwnershipGuard {

    private final SubmissionRepository submissionRepository;

    public SubmissionOwnershipGuard(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    public Result<Void> ensureOwner(
            UUID submissionId,
            UUID studentId
    ) {
        var submission = submissionRepository
                .findById(submissionId)
                .orElse(null);

        if (submission == null) {
            return Result.failure(
                    ApplicationError.notFound(
                            "entrega no encontrada"
                    )
            );
        }

        if (!submission.getStudentId().equals(studentId)) {
            return Result.failure(
                    ApplicationError.forbidden(
                            "la entrega no pertenece al estudiante"
                    )
            );
        }

        return Result.success(null);
    }
}
