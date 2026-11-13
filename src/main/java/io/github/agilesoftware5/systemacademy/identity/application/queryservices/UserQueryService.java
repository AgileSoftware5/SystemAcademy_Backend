package io.github.agilesoftware5.systemacademy.identity.application.queryservices;

import io.github.agilesoftware5.systemacademy.identity.application.dto.StudentProfile;
import io.github.agilesoftware5.systemacademy.identity.domain.model.aggregates.User;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetCurrentUserQuery;
import io.github.agilesoftware5.systemacademy.identity.domain.model.queries.GetStudentProfilesByIdsQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    /** Vacío si el usuario ya no existe o su cuenta dejó de estar activa: el token deja de valer. */
    Optional<User> handle(GetCurrentUserQuery query);

    List<StudentProfile> handle(GetStudentProfilesByIdsQuery query);
}
