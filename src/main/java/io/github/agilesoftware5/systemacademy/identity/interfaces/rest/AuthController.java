package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LoginCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.AuthenticatedSessionResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.LoginResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.RegisterUserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.UserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.AuthenticatedSessionResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.LoginCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.UserResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserCommandService registerUserCommandService;
    private final LoginCommandService loginCommandService;

    public AuthController(RegisterUserCommandService registerUserCommandService, LoginCommandService loginCommandService) {
        this.registerUserCommandService = registerUserCommandService;
        this.loginCommandService = loginCommandService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserResource resource) {
        var result = registerUserCommandService.handle(RegisterUserCommandFromResourceAssembler.toCommand(resource));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        UserResource body = UserResourceFromDomainAssembler.toResource(result.getValue());
        return ResponseEntity.status(201).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginResource resource) {
        var result = loginCommandService.handle(LoginCommandFromResourceAssembler.toCommand(resource));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        AuthenticatedSessionResource body = AuthenticatedSessionResourceFromDomainAssembler.toResource(result.getValue());
        return ResponseEntity.ok(body);
    }
}
