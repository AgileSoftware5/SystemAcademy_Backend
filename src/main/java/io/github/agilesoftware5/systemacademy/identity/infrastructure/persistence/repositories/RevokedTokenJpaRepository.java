package io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.entities.RevokedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface RevokedTokenJpaRepository extends JpaRepository<RevokedTokenEntity, String> {

    @Modifying
    @Query("delete from RevokedTokenEntity t where t.expiresAt < :now")
    void deleteByExpiresAtBefore(Instant now);
}
