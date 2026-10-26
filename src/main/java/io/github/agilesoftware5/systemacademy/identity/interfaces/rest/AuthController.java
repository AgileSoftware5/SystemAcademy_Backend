package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LoginCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetCurrentUserQuery;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.CurrentUserResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserCommandService registerUserCommandService;
    private final LoginCommandService loginCommandService;
    private final UserQueryService userQueryService;

    public AuthController(
            RegisterUserCommandService registerUserCommandService,
            LoginCommandService loginCommandService,
            UserQueryService userQueryService
    ) {
        this.registerUserCommandService = registerUserCommandService;
        this.loginCommandService = loginCommandService;
        this.userQueryService = userQueryService;
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

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado", description = "Se obtiene a partir del subject del JWT.")
    @ApiResponse(responseCode = "200", description = "Usuario de la sesión actual")
    @ApiResponse(responseCode = "401", description = "Sin token, token inválido/expirado o cuenta ya no activa", content = @Content)
    public ResponseEntity<?> me(Authentication authentication) {
        var user = userQueryService.handle(new GetCurrentUserQuery(AuthenticatedUser.id(authentication)));
        if (user.isEmpty()) {
            return ApplicationErrorHttpMapper.toResponse(ApplicationError.unauthorized("la sesión no es válida"));
        }
        return ResponseEntity.ok(CurrentUserResourceFromDomainAssembler.toResource(user.get()));
    }
}
