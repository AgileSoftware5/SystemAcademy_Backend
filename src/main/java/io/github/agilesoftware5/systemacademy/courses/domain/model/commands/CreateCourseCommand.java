package io.github.agilesoftware5.systemacademy.courses.domain.model.commands;

import java.util.UUID;

public record CreateCourseCommand(UUID instructorId, UUID createdBy, String code, String name, String description) {
}
