package io.github.agilesoftware5.systemacademy.identity.domain.model.queries;

/** Lista usuarios con filtros y paginación (uso exclusivo del administrador). */
public record ListUsersQuery(UserSearchCriteria criteria, int page, int size) {
}
