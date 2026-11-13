package io.github.agilesoftware5.systemacademy.courses.interfaces.rest;

import io.github.agilesoftware5.systemacademy.courses.application.commandservices.EnrollStudentCommandService;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources.EnrollStudentResource;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform.EnrollStudentCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.courses.interfaces.rest.transform.EnrollmentResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollStudentCommandService enrollStudentCommandService;

    public EnrollmentController(EnrollStudentCommandService enrollStudentCommandService) {
        this.enrollStudentCommandService = enrollStudentCommandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enroll(@Valid @RequestBody EnrollStudentResource resource, Authentication authentication) {
        UUID studentId = AuthenticatedUser.id(authentication);
        var command = EnrollStudentCommandFromResourceAssembler.toCommand(resource, studentId);
        var result = enrollStudentCommandService.handle(command);
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.status(201).body(EnrollmentResourceFromDomainAssembler.toResource(result.getValue()));
    }
}
