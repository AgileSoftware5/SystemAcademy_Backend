package io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.CourseStatus;

import java.util.Objects;
import java.util.UUID;

/** Aggregate root de un curso. El docente dueño lo crea y define su código de matrícula. */
public class Course {

    private final UUID id;
    private final UUID instructorId;
    private final UUID createdBy;
    private final String code;
    private final String name;
    private final String description;
    private final String enrollmentCode;
    private CourseStatus status;

    private Course(UUID id, UUID instructorId, UUID createdBy, String code, String name,
                    String description, String enrollmentCode, CourseStatus status) {
        this.id = id;
        this.instructorId = instructorId;
        this.createdBy = createdBy;
        this.code = code;
        this.name = name;
        this.description = description;
        this.enrollmentCode = enrollmentCode;
        this.status = status;
    }

    /** Alta de un curso nuevo: siempre nace ACTIVE. El enrollmentCode ya viene generado y validado como único. */
    public static Course create(UUID instructorId, UUID createdBy, String code, String name,
                                 String description, String enrollmentCode) {
        Objects.requireNonNull(instructorId, "instructorId no puede ser null");
        Objects.requireNonNull(createdBy, "createdBy no puede ser null");
        validateCode(code);
        validateName(name);
        Objects.requireNonNull(enrollmentCode, "enrollmentCode no puede ser null");
        return new Course(UUID.randomUUID(), instructorId, createdBy, code, name, description, enrollmentCode, CourseStatus.ACTIVE);
    }

    /** Reconstrucción desde persistencia: no revalida invariantes de negocio, ya son datos confiables. */
    public static Course fromPersistence(UUID id, UUID instructorId, UUID createdBy, String code, String name,
                                          String description, String enrollmentCode, CourseStatus status) {
        return new Course(id, instructorId, createdBy, code, name, description, enrollmentCode, status);
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }

    public boolean isActive() {
        return status == CourseStatus.ACTIVE;
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code no puede estar vacío");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacío");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEnrollmentCode() {
        return enrollmentCode;
    }

    public CourseStatus getStatus() {
        return status;
    }
}
