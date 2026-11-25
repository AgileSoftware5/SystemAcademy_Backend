package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest;

import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.UpdateOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.UpdateOpenQuestionResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.UpdateOpenQuestionCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.AddOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.CreateAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.PublishAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.ReleaseAssessmentResultsCommandService;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.UpdateAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.ReleaseAssessmentResultsCommand;
import io.github.agilesoftware5.systemacademy.assessments.application.queryservices.AssessmentQueryService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.GetAssessmentByIdQuery;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.ListAssessmentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AddOpenQuestionResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.CreateAssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.PublishAssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.UpdateAssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.AddOpenQuestionCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.AssessmentResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.CreateAssessmentCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.PublishAssessmentCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform.UpdateAssessmentCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.DeleteOpenQuestionCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.DeleteOpenQuestionCommand;
import io.github.agilesoftware5.systemacademy.assessments.application.commandservices.DeleteAssessmentCommandService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.DeleteAssessmentCommand;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AssessmentController {

    private final CreateAssessmentCommandService createAssessmentCommandService;
    private final AddOpenQuestionCommandService addOpenQuestionCommandService;
    private final UpdateOpenQuestionCommandService updateOpenQuestionCommandService;
    private final DeleteOpenQuestionCommandService deleteOpenQuestionCommandService;
    private final DeleteAssessmentCommandService deleteAssessmentCommandService;
    private final UpdateAssessmentCommandService updateAssessmentCommandService;
    private final PublishAssessmentCommandService publishAssessmentCommandService;
    private final ReleaseAssessmentResultsCommandService releaseAssessmentResultsCommandService;
    private final AssessmentQueryService assessmentQueryService;

    public AssessmentController(CreateAssessmentCommandService createAssessmentCommandService,
                                 AddOpenQuestionCommandService addOpenQuestionCommandService,
                                 UpdateOpenQuestionCommandService updateOpenQuestionCommandService,
                                DeleteOpenQuestionCommandService deleteOpenQuestionCommandService,
                                DeleteAssessmentCommandService deleteAssessmentCommandService,
                                 UpdateAssessmentCommandService updateAssessmentCommandService,
                                 PublishAssessmentCommandService publishAssessmentCommandService,
                                 ReleaseAssessmentResultsCommandService releaseAssessmentResultsCommandService,
                                 AssessmentQueryService assessmentQueryService) {
        this.createAssessmentCommandService = createAssessmentCommandService;
        this.addOpenQuestionCommandService = addOpenQuestionCommandService;
        this.updateOpenQuestionCommandService = updateOpenQuestionCommandService;
        this.updateAssessmentCommandService = updateAssessmentCommandService;
        this.publishAssessmentCommandService = publishAssessmentCommandService;
        this.releaseAssessmentResultsCommandService = releaseAssessmentResultsCommandService;
        this.assessmentQueryService = assessmentQueryService;
        this.deleteOpenQuestionCommandService = deleteOpenQuestionCommandService;
        this.deleteAssessmentCommandService = deleteAssessmentCommandService;
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

    @PutMapping("/api/v1/assessments/{id}/questions/{qid}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateOpenQuestion(
            @PathVariable UUID id,
            @PathVariable UUID qid,
            @Valid @RequestBody UpdateOpenQuestionResource resource,
            Authentication authentication) {

        UUID teacherId = AuthenticatedUser.id(authentication);

        var command = UpdateOpenQuestionCommandFromResourceAssembler
                .toCommand(resource, id, qid, teacherId);

        var result = updateOpenQuestionCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }

        return ResponseEntity.ok(
                AssessmentResourceFromDomainAssembler.toResource(
                        result.getValue(), true
                )
        );
    }

    @DeleteMapping("/api/v1/assessments/{id}/questions/{qid}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteOpenQuestion(
            @PathVariable UUID id,
            @PathVariable UUID qid,
            Authentication authentication) {

        UUID teacherId = AuthenticatedUser.id(authentication);

        var command = new DeleteOpenQuestionCommand(
                id,
                qid,
                teacherId
        );

        var result = deleteOpenQuestionCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(
                    result.getError()
            );
        }

        return ResponseEntity.ok(
                AssessmentResourceFromDomainAssembler.toResource(
                        result.getValue(),
                        true
                )
        );
    }

    @DeleteMapping("/api/v1/assessments/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteAssessment(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID teacherId = AuthenticatedUser.id(authentication);

        var command = new DeleteAssessmentCommand(id, teacherId);

        var result = deleteAssessmentCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(
                    result.getError()
            );
        }

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/assessments/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateDraft(@PathVariable UUID id, @Valid @RequestBody UpdateAssessmentResource resource,
                                          Authentication authentication) {
        UUID teacherId = AuthenticatedUser.id(authentication);
        var command = UpdateAssessmentCommandFromResourceAssembler.toCommand(resource, id, teacherId);
        var result = updateAssessmentCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.ok(AssessmentResourceFromDomainAssembler.toResource(result.getValue(), true));
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
