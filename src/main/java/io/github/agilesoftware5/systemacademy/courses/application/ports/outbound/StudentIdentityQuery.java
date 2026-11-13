package io.github.agilesoftware5.systemacademy.courses.application.ports.outbound;

import io.github.agilesoftware5.systemacademy.courses.application.dto.StudentProfile;

import java.util.List;
import java.util.UUID;

public interface StudentIdentityQuery {

    List<StudentProfile> findStudentProfilesByIds(List<UUID> studentIds);
}
