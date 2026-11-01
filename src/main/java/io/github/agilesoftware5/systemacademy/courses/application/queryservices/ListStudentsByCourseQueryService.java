package io.github.agilesoftware5.systemacademy.courses.application.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.dto.CourseStudent;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListStudentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;

import java.util.List;

public interface ListStudentsByCourseQueryService {

    Result<List<CourseStudent>> handle(ListStudentsByCourseQuery query);
}
