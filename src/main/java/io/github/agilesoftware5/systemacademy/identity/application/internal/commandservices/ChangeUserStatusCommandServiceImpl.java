package io.github.agilesoftware5.systemacademy.identity.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.ChangeUserStatusCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.ChangeUserStatusCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.UserRepository;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditEntry;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditLog;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChangeUserStatusCommandServiceImpl implements ChangeUserStatusCommandService {

    private static final Logger log = LoggerFactory.getLogger(ChangeUserStatusCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final AuditLog auditLog;

    public ChangeUserStatusCommandServiceImpl(UserRepository userRepository, AuditLog auditLog) {
        this.userRepository = userRepository;
        this.auditLog = auditLog;
    }

    @Override
    public Result<User> handle(ChangeUserStatusCommand command) {
        if (command.status() != UserStatus.ACTIVE && command.status() != UserStatus.SUSPENDED) {
            return Result.failure(ApplicationError.validationError("status debe ser ACTIVE o SUSPENDED"));
        }
        if (command.actorId().equals(command.targetId())) {
            return Result.failure(ApplicationError.businessRuleViolation("no puedes cambiar el estado de tu propia cuenta"));
        }
        var target = userRepository.findById(command.targetId());
        if (target.isEmpty()) {
            return Result.failure(ApplicationError.notFound("usuario no encontrado"));
        }
        var user = target.get();
        if (user.getRole() == Role.ADMIN) {
            return Result.failure(ApplicationError.businessRuleViolation("no se puede cambiar el estado de un administrador"));
        }
        if (user.getStatus() == command.status()) {
            return Result.success(user);
        }

        if (command.status() == UserStatus.SUSPENDED) {
            user.suspend();
        } else {
            user.activate();
        }
        var saved = userRepository.save(user);
        auditLog.record(new AuditEntry(
                command.actorId(),
                command.status() == UserStatus.SUSPENDED ? "USER_SUSPENDED" : "USER_ACTIVATED",
                "USER",
                saved.getId(),
                saved.getRole().name()));
        log.info("estado de usuario cambiado userId={} status={} actorId={}", saved.getId(), saved.getStatus(), command.actorId());
        return Result.success(saved);
    }
}
