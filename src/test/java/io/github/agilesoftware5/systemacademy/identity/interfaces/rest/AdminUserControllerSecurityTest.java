package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.ChangeUserStatusCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LoginCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LogoutCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.ListUsersQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.ChangeUserStatusCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.UserSearchCriteria;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.RevokedTokenRepository;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditLog;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.PageResult;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.security.JwtAuthenticationFilter;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.security.JwtService;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import io.github.agilesoftware5.systemacademy.shared.infrastructure.config.SecurityConfig;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.GlobalExceptionHandler;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.JsonAuthenticationEntryPoint;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Solo el ADMIN gestiona usuarios; el registro público ya no existe. */
@WebMvcTest(controllers = {AuthController.class, AdminUserController.class})
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, JsonAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-only-secret-with-at-least-32-characters",
        "app.jwt.expiration-minutes=60"
})
class AdminUserControllerSecurityTest {

    private static final String NEW_TEACHER = """
            {"email":"nuevo@academia.edu","password":"una-clave-larga-1","fullName":"Nuevo Docente","role":"TEACHER"}""";

    @Autowired
    WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter securityFilterChain;

    @Autowired
    JwtService jwtService;

    @MockitoBean
    UserQueryService userQueryService;

    @MockitoBean
    RegisterUserCommandService registerUserCommandService;

    @MockitoBean
    LoginCommandService loginCommandService;

    @MockitoBean
    LogoutCommandService logoutCommandService;

    @MockitoBean
    RevokedTokenRepository revokedTokenRepository;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    ChangeUserStatusCommandService changeUserStatusCommandService;

    @MockitoBean
    AuditLog auditLog;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
        when(userRepository.isActive(any())).thenReturn(true);
    }

    private String bearer(Role role) {
        return "Bearer " + jwtService.generate(UUID.randomUUID().toString(), role.name());
    }

    @Test
    void publicRegisterEndpointNoLongerExists() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(NEW_TEACHER))
                .andExpect(status().isNotFound());
        verify(registerUserCommandService, never()).handle(any());
    }

    @Test
    void adminCreatesATeacher() throws Exception {
        var created = User.register(new Email("nuevo@academia.edu"), "hash", "Nuevo Docente", Role.TEACHER);
        when(registerUserCommandService.handle(any(RegisterUserCommand.class))).thenReturn(Result.success(created));

        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(Role.ADMIN))
                        .contentType("application/json").content(NEW_TEACHER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void adminCannotCreateAnotherAdminThroughTheApi() throws Exception {
        var body = NEW_TEACHER.replace("TEACHER", "ADMIN");

        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(Role.ADMIN))
                        .contentType("application/json").content(body))
                .andExpect(status().is4xxClientError());
        verify(registerUserCommandService, never()).handle(any());
    }

    @Test
    void unknownRoleIsRejected() throws Exception {
        var body = NEW_TEACHER.replace("TEACHER", "DIRECTOR");

        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(Role.ADMIN))
                        .contentType("application/json").content(body))
                .andExpect(status().is4xxClientError());
        verify(registerUserCommandService, never()).handle(any());
    }

    @Test
    void teacherAndStudentAreForbidden() throws Exception {
        for (Role role : List.of(Role.TEACHER, Role.STUDENT)) {
            mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(role))
                            .contentType("application/json").content(NEW_TEACHER))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(role)))
                    .andExpect(status().isForbidden());
        }
        verify(registerUserCommandService, never()).handle(any());
    }

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminListsUsers() throws Exception {
        var user = User.register(new Email("ana@academia.edu"), "hash", "Ana", Role.STUDENT);
        when(userQueryService.handle(any(ListUsersQuery.class))).thenReturn(new PageResult<>(List.of(user), 1, 0, 20));

        mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email").value("ana@academia.edu"))
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listPassesFiltersAndCapsThePageSize() throws Exception {
        when(userQueryService.handle(any(ListUsersQuery.class))).thenReturn(new PageResult<>(List.of(), 0, 2, 100));

        mvc.perform(get("/api/v1/admin/users?q=ana&role=student&status=suspended&page=2&size=5000")
                        .header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isOk());

        verify(userQueryService).handle(argThat((ListUsersQuery q) ->
                q.page() == 2 && q.size() == 100
                        && q.criteria().equals(new UserSearchCriteria("ana", Role.STUDENT, UserStatus.SUSPENDED))));
    }

    @Test
    void invalidFilterIs400() throws Exception {
        mvc.perform(get("/api/v1/admin/users?role=director").header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminSuspendsAUser() throws Exception {
        var user = User.register(new Email("ana@academia.edu"), "hash", "Ana", Role.STUDENT);
        user.suspend();
        when(changeUserStatusCommandService.handle(any(ChangeUserStatusCommand.class))).thenReturn(Result.success(user));

        mvc.perform(patch("/api/v1/admin/users/" + user.getId() + "/status").header("Authorization", bearer(Role.ADMIN))
                        .contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void statusChangeRuleViolationIs422() throws Exception {
        when(changeUserStatusCommandService.handle(any(ChangeUserStatusCommand.class)))
                .thenReturn(Result.failure(ApplicationError.businessRuleViolation("no puedes cambiar el estado de tu propia cuenta")));

        mvc.perform(patch("/api/v1/admin/users/" + UUID.randomUUID() + "/status").header("Authorization", bearer(Role.ADMIN))
                        .contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void nonAdminsCannotChangeStatus() throws Exception {
        for (Role role : List.of(Role.TEACHER, Role.STUDENT)) {
            mvc.perform(patch("/api/v1/admin/users/" + UUID.randomUUID() + "/status").header("Authorization", bearer(role))
                            .contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                    .andExpect(status().isForbidden());
        }
        verify(changeUserStatusCommandService, never()).handle(any());
    }

    @Test
    void aTokenOfASuspendedAccountIsRejectedEvenIfStillValid() throws Exception {
        when(userRepository.isActive(any())).thenReturn(false);

        mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isUnauthorized());
    }
}
