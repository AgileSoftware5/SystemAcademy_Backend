package io.github.agilesoftware5.systemacademy.identity.domain.model.queries;

import java.util.List;
import java.util.UUID;

public record GetStudentProfilesByIdsQuery(
        List<UUID> studentIds
) {}
