package io.github.agilesoftware5.systemacademy.identity.interfaces.rest;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LoginCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.application.queryservices.UserQueryService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetCurrentUserQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.security.JwtAuthenticationFilter;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.security.JwtService;
import io.github.agilesoftware5.systemacademy.shared.infrastructure.config.SecurityConfig;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** BE-04: GET /api/v1/auth/me con el perímetro de seguridad real (SecurityConfig + filtro JWT). */
@WebMvcTest(AuthController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-only-secret-with-at-least-32-characters",
        "app.jwt.expiration-minutes=60"
})
class AuthControllerMeSecurityTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter securityFilterChain;

    MockMvc mvc;

    @Autowired
    JwtService jwtService;

    @MockitoBean
    UserQueryService userQueryService;

    @MockitoBean
    RegisterUserCommandService registerUserCommandService;

    @MockitoBean
    LoginCommandService loginCommandService;

    @BeforeEach
    void setUp() {
        // Solo la cadena de Spring Security, como en la app: el filtro JWT corre dentro de ella una sola vez.
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
    }

    private final User user = User.register(new Email("ana@academia.edu"), "hash", "Ana Pérez", Role.TEACHER);

    private String bearer() {
        return "Bearer " + jwtService.generate(user.getId().toString(), user.getRole().name());
    }

    @Test
    void validTokenReturnsTheAuthenticatedUser() throws Exception {
        when(userQueryService.handle(new GetCurrentUserQuery(user.getId()))).thenReturn(Optional.of(user));

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("ana@academia.edu"))
                .andExpect(jsonPath("$.fullName").value("Ana Pérez"))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void missingTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer no.es.un.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyIs401() throws Exception {
        var foreign = new JwtService(new io.github.agilesoftware5.systemacademy.identity.infrastructure.security.JwtProperties(
                "another-secret-with-at-least-32-characters!!", 60));
        var token = foreign.generate(user.getId().toString(), "TEACHER");

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenOfAnInactiveOrMissingUserIs401() throws Exception {
        when(userQueryService.handle(any(GetCurrentUserQuery.class))).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void otherAuthEndpointsStayPublic() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
