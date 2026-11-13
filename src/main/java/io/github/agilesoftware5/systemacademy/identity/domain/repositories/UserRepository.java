package io.github.agilesoftware5.systemacademy.identity.domain.repositories;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto del dominio: la infraestructura lo implementa, el dominio solo lo consume. */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    List<User> findByIdsAndRoleAndStatus(
            List<UUID> ids,
            Role role,
            UserStatus status
    );
}
