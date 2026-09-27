package io.github.agilesoftware5.systemacademy.courses.domain.model.commands;

import java.util.UUID;

public record EnrollStudentCommand(String enrollmentCode, UUID studentId) {
}
