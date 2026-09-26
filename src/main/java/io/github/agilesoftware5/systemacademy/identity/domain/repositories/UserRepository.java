package io.github.agilesoftware5.systemacademy.identity.domain.repositories;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;

import java.util.Optional;
import java.util.UUID;

/** Puerto del dominio: la infraestructura lo implementa, el dominio solo lo consume. */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
