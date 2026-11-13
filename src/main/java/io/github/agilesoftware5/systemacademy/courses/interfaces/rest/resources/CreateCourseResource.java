package io.github.agilesoftware5.systemacademy.courses.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseResource(@NotBlank String code, @NotBlank String name, String description) {
}
