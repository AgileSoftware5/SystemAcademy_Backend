package io.github.agilesoftware5.systemacademy.courses.interfaces.rest;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByStudentQuery;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CourseResource;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform.CourseResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me/courses")
public class StudentCoursesController {

    private final CourseQueryService courseQueryService;

    public StudentCoursesController(CourseQueryService courseQueryService) {
        this.courseQueryService = courseQueryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResource>> getMyCourses(
            Authentication authentication) {

        UUID studentId = AuthenticatedUser.id(authentication);

        List<Course> courses = courseQueryService.handle(
                new ListCoursesByStudentQuery(studentId)
        );

        var resources = courses.stream()
                .map(CourseResourceFromDomainAssembler::toResource)
                .toList();

        return ResponseEntity.ok(resources);
    }
}
