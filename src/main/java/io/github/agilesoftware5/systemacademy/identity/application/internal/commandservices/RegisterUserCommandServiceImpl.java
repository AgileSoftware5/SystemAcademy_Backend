package io.github.agilesoftware5.systemacademy.identity.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.RegisterUserCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.RegisterUserCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.identity.domain.services.PasswordEncoder;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserCommandServiceImpl implements RegisterUserCommandService {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserCommandServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result<User> handle(RegisterUserCommand command) {
        log.debug("registrando usuario email={} role={}", command.email(), command.role());
        try {
            Email email = new Email(command.email());
            if (userRepository.existsByEmail(email)) {
                log.warn("intento de registro con email duplicado: {}", command.email());
                return Result.failure(ApplicationError.conflict("ya existe un usuario con ese email"));
            }
            var user = User.register(
                    email,
                    passwordEncoder.encode(command.rawPassword()),
                    command.fullName(),
                    command.role()
            );
            var saved = userRepository.save(user);
            log.info("usuario registrado id={} role={}", saved.getId(), saved.getRole());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("datos de registro inválidos: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError(ex.getMessage()));
        }
    }
}
