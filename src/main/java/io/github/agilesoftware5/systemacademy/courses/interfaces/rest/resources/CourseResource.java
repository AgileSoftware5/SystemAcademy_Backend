package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources;

import java.util.UUID;

public record CourseResource(
        UUID id,
        UUID instructorId,
        String code,
        String name,
        String description,
        String enrollmentCode,
        String status
) {
}
