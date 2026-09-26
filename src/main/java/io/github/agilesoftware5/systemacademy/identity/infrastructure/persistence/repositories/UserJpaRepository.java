package io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.repositories;

import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
