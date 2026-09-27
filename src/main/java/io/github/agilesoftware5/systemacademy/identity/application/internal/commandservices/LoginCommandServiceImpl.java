package io.github.agilesoftware5.systemacademy.identity.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LoginCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.LoginCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.AuthenticatedSession;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.identity.domain.services.PasswordEncoder;
import io.github.agilesoftware5.systemacademy.identity.domain.services.TokenIssuer;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginCommandServiceImpl implements LoginCommandService {

    private static final Logger log = LoggerFactory.getLogger(LoginCommandServiceImpl.class);
    private static final String INVALID_CREDENTIALS = "email o contraseña inválidos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public LoginCommandServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Result<AuthenticatedSession> handle(LoginCommand command) {
        log.debug("intento de login email={}", command.email());

        Email email;
        try {
            email = new Email(command.email());
        } catch (IllegalArgumentException ex) {
            // formato inválido no debe distinguirse de credenciales incorrectas (evita enumeración de usuarios).
            log.warn("login con formato de email inválido: {}", command.email());
            return Result.failure(ApplicationError.businessRuleViolation(INVALID_CREDENTIALS));
        }

        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
            log.warn("login fallido para email={}", command.email());
            return Result.failure(ApplicationError.businessRuleViolation(INVALID_CREDENTIALS));
        }
        if (!user.isActive()) {
            log.warn("login rechazado, cuenta no activa userId={}", user.getId());
            return Result.failure(ApplicationError.businessRuleViolation("la cuenta no está activa"));
        }
        var token = tokenIssuer.issue(user);
        log.info("login exitoso userId={} role={}", user.getId(), user.getRole());
        return Result.success(new AuthenticatedSession(token, user.getId(), user.getRole()));
    }
}
