package io.github.agilesoftware5.systemacademy.submissions.domain.model.queries;

import java.util.UUID;

public record ListSubmissionsByStudentQuery(
        UUID studentId
) {
}
