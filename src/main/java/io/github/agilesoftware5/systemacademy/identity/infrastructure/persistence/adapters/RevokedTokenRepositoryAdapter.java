package io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.identity.domain.repositories.RevokedTokenRepository;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.entities.RevokedTokenEntity;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.repositories.RevokedTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Adapter: implementa el puerto {@link RevokedTokenRepository} usando Spring Data JPA. */
@Repository
public class RevokedTokenRepositoryAdapter implements RevokedTokenRepository {

    private final RevokedTokenJpaRepository jpaRepository;

    public RevokedTokenRepositoryAdapter(RevokedTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void revoke(String tokenId, Instant expiresAt) {
        jpaRepository.save(new RevokedTokenEntity(tokenId, expiresAt));
    }

    @Override
    public boolean isRevoked(String tokenId) {
        return jpaRepository.existsById(tokenId);
    }

    @Override
    @Transactional
    public void deleteExpired(Instant now) {
        jpaRepository.deleteByExpiresAtBefore(now);
    }
}
