package io.github.agilesoftware5.systemacademy.assessments.application.queryservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.GetAssessmentByIdQuery;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.ListAssessmentsByCourseQuery;

import java.util.List;
import java.util.Optional;

public interface AssessmentQueryService {

    Optional<Assessment> handle(GetAssessmentByIdQuery query);

    List<Assessment> handle(ListAssessmentsByCourseQuery query);
}
