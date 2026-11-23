package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.ListUsersQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.RegisterUserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.UserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.UserResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de usuarios: no hay registro público, solo el administrador crea cuentas de docente y alumno.
 * El primer administrador se crea al arrancar (ver AdminBootstrapRunner) y nunca por la API.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final RegisterUserCommandService registerUserCommandService;
    private final UserQueryService userQueryService;

    public AdminUserController(RegisterUserCommandService registerUserCommandService, UserQueryService userQueryService) {
        this.registerUserCommandService = registerUserCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping
    @Operation(summary = "Crea un docente o un alumno", description = "Solo ADMIN. El rol ADMIN no se puede crear por la API.")
    public ResponseEntity<?> create(@Valid @RequestBody RegisterUserResource resource) {
        Role role;
        try {
            role = Role.valueOf(resource.role().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ApplicationErrorHttpMapper.toResponse(ApplicationError.validationError("role debe ser TEACHER o STUDENT"));
        }
        if (role == Role.ADMIN) {
            return ApplicationErrorHttpMapper.toResponse(ApplicationError.validationError("role debe ser TEACHER o STUDENT"));
        }

        var result = registerUserCommandService.handle(RegisterUserCommandFromResourceAssembler.toCommand(resource));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.status(201).body(UserResourceFromDomainAssembler.toResource(result.getValue()));
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios", description = "Solo ADMIN.")
    public ResponseEntity<List<UserResource>> list() {
        var users = userQueryService.handle(new ListUsersQuery()).stream()
                .map(UserResourceFromDomainAssembler::toResource)
                .toList();
        return ResponseEntity.ok(users);
    }
}
