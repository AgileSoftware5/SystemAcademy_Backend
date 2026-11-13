package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.AddOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.CreateAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.PublishAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.ReleaseAssessmentResultsCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.ReleaseAssessmentResultsCommand;
import io.github.agilesoftware5.systemacademy.assessments.application.queryservices.AssessmentQueryService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.GetAssessmentByIdQuery;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.ListAssessmentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AddOpenQuestionResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.CreateAssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.PublishAssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.AddOpenQuestionCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.AssessmentResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.CreateAssessmentCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.PublishAssessmentCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AssessmentController {

    private final CreateAssessmentCommandService createAssessmentCommandService;
    private final AddOpenQuestionCommandService addOpenQuestionCommandService;
    private final PublishAssessmentCommandService publishAssessmentCommandService;
    private final ReleaseAssessmentResultsCommandService releaseAssessmentResultsCommandService;
    private final AssessmentQueryService assessmentQueryService;

    public AssessmentController(CreateAssessmentCommandService createAssessmentCommandService,
                                 AddOpenQuestionCommandService addOpenQuestionCommandService,
                                 PublishAssessmentCommandService publishAssessmentCommandService,
                                 ReleaseAssessmentResultsCommandService releaseAssessmentResultsCommandService,
                                 AssessmentQueryService assessmentQueryService) {
        this.createAssessmentCommandService = createAssessmentCommandService;
        this.addOpenQuestionCommandService = addOpenQuestionCommandService;
        this.publishAssessmentCommandService = publishAssessmentCommandService;
        this.releaseAssessmentResultsCommandService = releaseAssessmentResultsCommandService;
        this.assessmentQueryService = assessmentQueryService;
    }

    @PostMapping("/api/v1/courses/{courseId}/assessments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> create(@PathVariable UUID courseId, @Valid @RequestBody CreateAssessmentResource resource,
                                     Authentication authentication) {
        UUID teacherId = AuthenticatedUser.id(authentication);
        var command = CreateAssessmentCommandFromResourceAssembler.toCommand(resource, courseId, teacherId);
        var result = createAssessmentCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.status(201).body(AssessmentResourceFromDomainAssembler.toResource(result.getValue(), true));
    }

    @GetMapping("/api/v1/courses/{courseId}/assessments")
    public ResponseEntity<List<AssessmentResource>> listByCourse(@PathVariable UUID courseId, Authentication authentication) {
        boolean includeModelAnswer = AuthenticatedUser.hasRole(authentication, "TEACHER") || AuthenticatedUser.hasRole(authentication, "ADMIN");
        var assessments = assessmentQueryService.handle(new ListAssessmentsByCourseQuery(courseId));
        return ResponseEntity.ok(assessments.stream()
                .map(a -> AssessmentResourceFromDomainAssembler.toResource(a, includeModelAnswer))
                .toList());
    }

    @PostMapping("/api/v1/assessments/{id}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> addOpenQuestion(@PathVariable UUID id, @Valid @RequestBody AddOpenQuestionResource resource,
                                              Authentication authentication) {
        UUID teacherId = AuthenticatedUser.id(authentication);
        var command = AddOpenQuestionCommandFromResourceAssembler.toCommand(resource, id, teacherId);
        var result = addOpenQuestionCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.status(201).body(AssessmentResourceFromDomainAssembler.toResource(result.getValue(), true));
    }

    @PostMapping("/api/v1/assessments/{id}/publications")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> publish(@PathVariable UUID id, @Valid @RequestBody PublishAssessmentResource resource,
                                      Authentication authentication) {
        UUID teacherId = AuthenticatedUser.id(authentication);
        var command = PublishAssessmentCommandFromResourceAssembler.toCommand(resource, id, teacherId);
        var result = publishAssessmentCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.ok(AssessmentResourceFromDomainAssembler.toResource(result.getValue(), true));
    }

    @PostMapping("/api/v1/assessments/{id}/result-releases")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> releaseResults(@PathVariable UUID id, Authentication authentication) {
        UUID teacherId = AuthenticatedUser.id(authentication);
        var result = releaseAssessmentResultsCommandService.handle(new ReleaseAssessmentResultsCommand(id, teacherId));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.ok(AssessmentResourceFromDomainAssembler.toResource(result.getValue(), true));
    }

    @GetMapping("/api/v1/assessments/{id}")
    public ResponseEntity<AssessmentResource> getById(@PathVariable UUID id, Authentication authentication) {
        boolean includeModelAnswer = AuthenticatedUser.hasRole(authentication, "TEACHER") || AuthenticatedUser.hasRole(authentication, "ADMIN");
        return assessmentQueryService.handle(new GetAssessmentByIdQuery(id))
                .map(a -> AssessmentResourceFromDomainAssembler.toResource(a, includeModelAnswer))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
