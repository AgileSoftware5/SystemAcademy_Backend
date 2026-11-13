package io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.infrastructure.persistence.entities.UserEntity;

/** Traduce entre el aggregate de dominio {@link User} y su entidad JPA. */
public final class UserAssembler {

    private UserAssembler() {
    }

    public static User toDomain(UserEntity entity) {
        return User.fromPersistence(
                entity.getId(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    public static UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
