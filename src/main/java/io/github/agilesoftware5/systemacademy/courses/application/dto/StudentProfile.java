package io.github.agilesoftware5.systemacademy.courses.application.dto;

import java.util.UUID;

public record StudentProfile(
        UUID studentId,
        String fullName,
        String email
) {}
