package io.github.agilesoftware5.systemacademy.identity.infrastructure.bootstrap;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador al arrancar, una sola vez: si ya existe algún ADMIN no hace nada.
 * Se activa con ADMIN_EMAIL y ADMIN_PASSWORD; después de la primera ejecución se pueden quitar.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    static final int MIN_PASSWORD_LENGTH = 12;

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final RegisterUserCommandService registerUserCommandService;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            RegisterUserCommandService registerUserCommandService,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.full-name:Administrador}") String fullName
    ) {
        this.userRepository = userRepository;
        this.registerUserCommandService = registerUserCommandService;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("administrador ya existe: no se crea otro");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        var result = registerUserCommandService.handle(new RegisterUserCommand(email, password, fullName, Role.ADMIN));
        if (result.isFailure()) {
            throw new IllegalStateException("no se pudo crear el administrador inicial: " + result.getError().message());
        }
        log.info("administrador inicial creado id={}", result.getValue().getId());
    }
}
