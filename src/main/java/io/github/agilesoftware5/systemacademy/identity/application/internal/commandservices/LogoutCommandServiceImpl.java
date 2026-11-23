package io.github.agilesoftware5.systemacademy.identity.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.identity.application.commandservices.LogoutCommandService;
import io.github.agilesoftware5.systemacademy.identity.domain.model.commands.LogoutCommand;
import io.github.agilesoftware5.systemacademy.identity.domain.repositories.RevokedTokenRepository;
import io.github.agilesoftware5.systemacademy.shared.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LogoutCommandServiceImpl implements LogoutCommandService {

    private static final Logger log = LoggerFactory.getLogger(LogoutCommandServiceImpl.class);

    private final RevokedTokenRepository revokedTokenRepository;

    public LogoutCommandServiceImpl(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public Result<Void> handle(LogoutCommand command) {
        // Cerrar sesión es idempotente: un token sin id (emitido antes de existir el logout) solo expira.
        if (command.tokenId() == null) {
            log.debug("logout de un token sin id, se deja expirar");
            return Result.success(null);
        }

        revokedTokenRepository.revoke(command.tokenId(), command.tokenExpiresAt());
        revokedTokenRepository.deleteExpired(Instant.now());
        log.info("sesión cerrada tokenId={}", command.tokenId());
        return Result.success(null);
    }
}
