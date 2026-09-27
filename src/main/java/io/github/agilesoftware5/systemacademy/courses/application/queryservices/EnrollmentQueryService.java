package io.github.agilesoftware5.systemacademy.courses.application.queryservices;

import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.IsStudentEnrolledQuery;

public interface EnrollmentQueryService {

    boolean handle(IsStudentEnrolledQuery query);
}
