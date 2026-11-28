package io.github.agilesoftware5.systemacademy.identity.domain.model.queries;

import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.Role;
import io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects.UserStatus;

/** Todos los campos son opcionales: null o vacío significa "sin filtro". */
public record UserSearchCriteria(String text, Role role, UserStatus status) {
}
