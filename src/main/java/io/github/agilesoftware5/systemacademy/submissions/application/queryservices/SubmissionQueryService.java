package io.github.agilesoftware5.systemacademy.submissions.application.queryservices;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.AreResultsReleasedQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.GetSubmissionByIdQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByAssessmentQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByStudentQuery;

import java.util.List;
import java.util.Optional;

public interface SubmissionQueryService {

    Optional<Submission> handle(GetSubmissionByIdQuery query);

    List<Submission> handle(ListSubmissionsByStudentQuery query);

    /** Vacío si quien consulta no es el docente de la evaluación. */
    List<Submission> handle(ListSubmissionsByAssessmentQuery query);

    boolean handle(AreResultsReleasedQuery query);
}
