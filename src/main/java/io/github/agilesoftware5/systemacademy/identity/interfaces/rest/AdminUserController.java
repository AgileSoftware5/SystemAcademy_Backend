package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.ChangeUserStatusCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.ChangeUserStatusCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.ListUsersQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.UserSearchCriteria;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.ChangeUserStatusResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.RegisterUserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.resources.UserResource;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import io.github.agilesoftware5.systemacademy.identity.interfaces.rest.transform.UserResourceFromDomainAssembler;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditEntry;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditLog;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.ApplicationErrorHttpMapper;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.AuthenticatedUser;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.resources.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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
    private final ChangeUserStatusCommandService changeUserStatusCommandService;
    private final AuditLog auditLog;

    public AdminUserController(
            RegisterUserCommandService registerUserCommandService,
            UserQueryService userQueryService,
            ChangeUserStatusCommandService changeUserStatusCommandService,
            AuditLog auditLog
    ) {
        this.registerUserCommandService = registerUserCommandService;
        this.userQueryService = userQueryService;
        this.changeUserStatusCommandService = changeUserStatusCommandService;
        this.auditLog = auditLog;
    }

    @PostMapping
    @Operation(summary = "Crea un docente o un alumno", description = "Solo ADMIN. El rol ADMIN no se puede crear por la API.")
    public ResponseEntity<?> create(@Valid @RequestBody RegisterUserResource resource, Authentication authentication) {
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
        var created = result.getValue();
        auditLog.record(new AuditEntry(AuthenticatedUser.id(authentication), "USER_CREATED", "USER", created.getId(), created.getRole().name()));
        return ResponseEntity.status(201).body(UserResourceFromDomainAssembler.toResource(created));
    }

    @GetMapping
    @Operation(summary = "Lista usuarios con búsqueda, filtros y paginación", description = "Solo ADMIN. Filtros opcionales: q (nombre o correo), role, status.")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Role roleFilter;
        UserStatus statusFilter;
        try {
            roleFilter = role == null || role.isBlank() ? null : Role.valueOf(role.trim().toUpperCase());
            statusFilter = status == null || status.isBlank() ? null : UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ApplicationErrorHttpMapper.toResponse(ApplicationError.validationError("role o status no válido"));
        }
        var result = userQueryService.handle(new ListUsersQuery(
                new UserSearchCriteria(q, roleFilter, statusFilter),
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100)));
        return ResponseEntity.ok(PageResource.from(result, UserResourceFromDomainAssembler::toResource));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Suspende o reactiva una cuenta", description = "Solo ADMIN. status: ACTIVE o SUSPENDED. Una cuenta suspendida deja de poder usar su sesión.")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeUserStatusResource resource, Authentication authentication) {
        UserStatus status;
        try {
            status = UserStatus.valueOf(resource.status().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ApplicationErrorHttpMapper.toResponse(ApplicationError.validationError("status debe ser ACTIVE o SUSPENDED"));
        }
        var result = changeUserStatusCommandService.handle(new ChangeUserStatusCommand(AuthenticatedUser.id(authentication), id, status));
        if (result.isFailure()) {
            return ApplicationErrorHttpMapper.toResponse(result.getError());
        }
        return ResponseEntity.ok(UserResourceFromDomainAssembler.toResource(result.getValue()));
    }
}
