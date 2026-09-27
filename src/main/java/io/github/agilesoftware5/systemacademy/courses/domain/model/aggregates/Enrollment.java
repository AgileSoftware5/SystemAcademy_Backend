package io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Aggregate root de una matrícula. Referencia a Course y a un alumno solo por id (aggregates independientes). */
public class Enrollment {

    private final UUID id;
    private final UUID courseId;
    private final UUID studentId;
    private final Instant enrolledAt;
    private EnrollmentStatus status;

    private Enrollment(UUID id, UUID courseId, UUID studentId, Instant enrolledAt, EnrollmentStatus status) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrolledAt = enrolledAt;
        this.status = status;
    }

    /** Matrícula nueva: siempre nace ACTIVE, enrolledAt es el instante de la matriculación (no un timestamp de auditoría). */
    public static Enrollment enroll(UUID courseId, UUID studentId) {
        Objects.requireNonNull(courseId, "courseId no puede ser null");
        Objects.requireNonNull(studentId, "studentId no puede ser null");
        return new Enrollment(UUID.randomUUID(), courseId, studentId, Instant.now(), EnrollmentStatus.ACTIVE);
    }

    /** Reconstrucción desde persistencia. */
    public static Enrollment fromPersistence(UUID id, UUID courseId, UUID studentId, Instant enrolledAt, EnrollmentStatus status) {
        return new Enrollment(id, courseId, studentId, enrolledAt, status);
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }
}
