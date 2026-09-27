package io.github.agilesoftware5.systemacademy.courses.application.queryservices;

import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;

import java.util.List;
import java.util.Optional;

public interface CourseQueryService {

    Optional<Course> handle(GetCourseByIdQuery query);

    List<Course> handle(ListCoursesByInstructorQuery query);
}
