package io.github.agilesoftware5.systemacademy.courses.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CourseStudent(
        UUID studentId,
        String fullName,
        String email,
        Instant enrolledAt
) {}