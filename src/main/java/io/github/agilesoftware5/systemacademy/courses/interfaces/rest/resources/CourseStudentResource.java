package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record CourseStudentResource(
        UUID studentId,
        String fullName,
        String email,
        Instant enrolledAt
) {
}
