package io.github.agilesoftware5.systemacademy.identity.domain.repositories;

import java.time.Instant;

/** Puerto del dominio: tokens que ya no deben aceptarse aunque su firma y expiración sigan siendo válidas. */
public interface RevokedTokenRepository {

    void revoke(String tokenId, Instant expiresAt);

    boolean isRevoked(String tokenId);

    /** Un token vencido ya es rechazado por su expiración, así que su revocación deja de hacer falta. */
    void deleteExpired(Instant now);
}
