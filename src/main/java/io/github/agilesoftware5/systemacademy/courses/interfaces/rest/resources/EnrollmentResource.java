package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources;

import java.util.UUID;

public record EnrollmentResource(UUID id, UUID courseId, UUID studentId, String status) {
}
