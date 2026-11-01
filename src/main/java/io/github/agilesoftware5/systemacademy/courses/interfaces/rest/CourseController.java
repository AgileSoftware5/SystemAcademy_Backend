package io.github.agilesoftware5.systemacademy.courses.interfaces.rest;

import io.github.agilesoftware5.systemacademy.courses.application.commandservices.CreateCourseCommandService;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.ListStudentsByCourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListStudentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CourseResource;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CourseStudentResource;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.CreateCourseResource;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform.CourseResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform.CreateCourseCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CreateCourseCommandService createCourseCommandService;
    private final CourseQueryService courseQueryService;
    private final ListStudentsByCourseQueryService listStudentsByCourseQueryService;

    public CourseController(CreateCourseCommandService createCourseCommandService,
                            CourseQueryService courseQueryService,
                            ListStudentsByCourseQueryService listStudentsByCourseQueryService) {
        this.createCourseCommandService = createCourseCommandService;
        this.courseQueryService = courseQueryService;
        this.listStudentsByCourseQueryService = listStudentsByCourseQueryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCourseResource resource, Authentication authentication) {
        UUID instructorId = AuthenticatedUser.id(authentication);
        var command = CreateCourseCommandFromResourceAssembler.toCommand(resource, instructorId);
        var result = createCourseCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.status(201).body(CourseResourceFromDomainAssembler.toResource(result.getValue()));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseResource>> listMine(Authentication authentication) {
        UUID instructorId = AuthenticatedUser.id(authentication);
        var courses = courseQueryService.handle(new ListCoursesByInstructorQuery(instructorId));
        return ResponseEntity.ok(courses.stream().map(CourseResourceFromDomainAssembler::toResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResource> getById(@PathVariable UUID id) {
        return courseQueryService.handle(new GetCourseByIdQuery(id))
                .map(CourseResourceFromDomainAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getStudentsByCourse(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID teacherId = AuthenticatedUser.id(authentication);

        var query = new ListStudentsByCourseQuery(id, teacherId);

        var result = listStudentsByCourseQueryService.handle(query);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }

        var response = result.getValue().stream()
                .map(student -> new CourseStudentResource(
                        student.studentId(),
                        student.fullName(),
                        student.email(),
                        student.enrolledAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
