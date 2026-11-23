package io.github.agilesoftware5.systemacademy.identity.infrastructure.bootstrap;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapRunnerTest {

    private static final String STRONG = "una-clave-bien-larga-1";

    private final UserRepository users = mock(UserRepository.class);
    private final RegisterUserCommandService register = mock(RegisterUserCommandService.class);

    private AdminBootstrapRunner runner(String email, String password) {
        return new AdminBootstrapRunner(users, register, email, password, "Administrador");
    }

    @Test
    void doesNothingWithoutCredentials() {
        runner("", "").run(null);
        runner("admin@x.dev", "").run(null);
        verify(register, never()).handle(any());
    }

    @Test
    void createsTheFirstAdmin() {
        var admin = User.register(new Email("admin@x.dev"), "hash", "Administrador", Role.ADMIN);
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);
        when(register.handle(any(RegisterUserCommand.class))).thenReturn(Result.success(admin));

        runner("admin@x.dev", STRONG).run(null);

        verify(register).handle(new RegisterUserCommand("admin@x.dev", STRONG, "Administrador", Role.ADMIN));
    }

    @Test
    void doesNotCreateASecondAdmin() {
        when(users.existsByRole(Role.ADMIN)).thenReturn(true);

        runner("otro@x.dev", STRONG).run(null);

        verify(register, never()).handle(any());
    }

    @Test
    void rejectsAWeakPassword() {
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> runner("admin@x.dev", "corta").run(null));
        verify(register, never()).handle(any());
    }

    @Test
    void failsLoudlyIfCreationFails() {
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);
        when(register.handle(any(RegisterUserCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("ya existe")));

        assertThrows(IllegalStateException.class, () -> runner("admin@x.dev", STRONG).run(null));
    }
}
