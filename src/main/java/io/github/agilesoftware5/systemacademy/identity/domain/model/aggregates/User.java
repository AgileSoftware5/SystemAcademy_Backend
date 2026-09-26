package io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Email;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root de un usuario del sistema (docente, alumno o admin).
 * No conoce JPA ni ningún detalle de persistencia.
 */
public class User {

    private final UUID id;
    private final Email email;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private UserStatus status;

    private User(UUID id, Email email, String passwordHash, String fullName, Role role, UserStatus status) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    /** Alta de un usuario nuevo: siempre nace ACTIVE. Email ya validado por el value object. */
    public static User register(Email email, String passwordHash, String fullName, Role role) {
        Objects.requireNonNull(email, "email no puede ser null");
        validatePasswordHash(passwordHash);
        validateFullName(fullName);
        Objects.requireNonNull(role, "role no puede ser null");
        return new User(UUID.randomUUID(), email, passwordHash, fullName, role, UserStatus.ACTIVE);
    }

    /** Reconstrucción desde persistencia: no revalida invariantes de negocio, ya son datos confiables. */
    public static User fromPersistence(UUID id, Email email, String passwordHash, String fullName, Role role, UserStatus status) {
        return new User(id, email, passwordHash, fullName, role, status);
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private static void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash no puede estar vacío");
        }
    }

    private static void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName no puede estar vacío");
        }
    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }
}
