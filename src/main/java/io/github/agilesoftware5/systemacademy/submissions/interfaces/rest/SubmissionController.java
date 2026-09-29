package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest;

import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.AddAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.StartSubmissionCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.SubmitSubmissionCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.commandservices.UpdateAnswerCommandService;
import io.github.agilesoftware5.systemacademy.submissions.application.queryservices.SubmissionQueryService;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.StartSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.SubmitSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.GetSubmissionByIdQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByAssessmentQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByStudentQuery;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.AddAnswerResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.SubmissionResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.resources.UpdateAnswerResource;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform.AddAnswerCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform.SubmissionResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform.UpdateAnswerCommandFromResourceAssembler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SubmissionController {

    private final StartSubmissionCommandService startSubmissionCommandService;
    private final AddAnswerCommandService addAnswerCommandService;
    private final UpdateAnswerCommandService updateAnswerCommandService;
    private final SubmitSubmissionCommandService submitSubmissionCommandService;
    private final SubmissionQueryService submissionQueryService;

    public SubmissionController(
            StartSubmissionCommandService startSubmissionCommandService,
            AddAnswerCommandService addAnswerCommandService,
            UpdateAnswerCommandService updateAnswerCommandService,
            SubmitSubmissionCommandService submitSubmissionCommandService,
            SubmissionQueryService submissionQueryService
    ) {
        this.startSubmissionCommandService = startSubmissionCommandService;
        this.addAnswerCommandService = addAnswerCommandService;
        this.updateAnswerCommandService = updateAnswerCommandService;
        this.submitSubmissionCommandService = submitSubmissionCommandService;
        this.submissionQueryService = submissionQueryService;
    }

    @PostMapping("/api/v1/assessments/{assessmentId}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> start(
            @PathVariable UUID assessmentId,
            Authentication authentication
    ) {
        UUID studentId = AuthenticatedUser.id(authentication);

        var command = new StartSubmissionCommand(
                assessmentId,
                studentId
        );

        var result = startSubmissionCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }

        return ResponseEntity
                .status(201)
                .body(
                        SubmissionResourceFromDomainAssembler.toResource(
                                result.getValue()
                        )
                );
    }

    @PostMapping("/api/v1/submissions/{id}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> addAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody AddAnswerResource resource,
            Authentication authentication
    ) {
        UUID studentId = AuthenticatedUser.id(authentication);

        var command =
                AddAnswerCommandFromResourceAssembler.toCommand(
                        resource,
                        id,
                        studentId
                );

        var result = addAnswerCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(
                    result.getError()
            );
        }

        return ResponseEntity
                .status(201)
                .body(
                        SubmissionResourceFromDomainAssembler.toResource(
                                result.getValue()
                        )
                );
    }

    @PutMapping("/api/v1/submissions/{submissionId}/answers/{answerId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateAnswer(
            @PathVariable UUID submissionId,
            @PathVariable UUID answerId,
            @Valid @RequestBody UpdateAnswerResource resource,
            Authentication authentication
    ) {
        UUID studentId = AuthenticatedUser.id(authentication);

        var command =
                UpdateAnswerCommandFromResourceAssembler.toCommand(
                        resource,
                        submissionId,
                        studentId,
                        answerId
                );

        var result = updateAnswerCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(
                    result.getError()
            );
        }

        return ResponseEntity.ok(
                SubmissionResourceFromDomainAssembler.toResource(
                        result.getValue()
                )
        );
    }

    @PostMapping("/api/v1/submissions/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID studentId = AuthenticatedUser.id(authentication);

        var command = new SubmitSubmissionCommand(
                id,
                studentId
        );

        var result = submitSubmissionCommandService.handle(command);

        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(
                    result.getError()
            );
        }

        return ResponseEntity.ok(
                SubmissionResourceFromDomainAssembler.toResource(
                        result.getValue()
                )
        );
    }

    @GetMapping("/api/v1/submissions/{id}")
    public ResponseEntity<SubmissionResource> getById(
            @PathVariable UUID id
    ) {
        return submissionQueryService
                .handle(new GetSubmissionByIdQuery(id))
                .map(SubmissionResourceFromDomainAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/students/{studentId}/submissions")
    public ResponseEntity<List<SubmissionResource>> listByStudent(
            @PathVariable UUID studentId
    ) {
        var submissions =
                submissionQueryService.handle(
                        new ListSubmissionsByStudentQuery(studentId)
                );

        return ResponseEntity.ok(
                submissions.stream()
                        .map(SubmissionResourceFromDomainAssembler::toResource)
                        .toList()
        );
    }

    @GetMapping("/api/v1/assessments/{assessmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SubmissionResource>> listByAssessment(
            @PathVariable UUID assessmentId
    ) {
        var submissions =
                submissionQueryService.handle(
                        new ListSubmissionsByAssessmentQuery(
                                assessmentId
                        )
                );

        return ResponseEntity.ok(
                submissions.stream()
                        .map(SubmissionResourceFromDomainAssembler::toResource)
                        .toList()
        );
    }
}
