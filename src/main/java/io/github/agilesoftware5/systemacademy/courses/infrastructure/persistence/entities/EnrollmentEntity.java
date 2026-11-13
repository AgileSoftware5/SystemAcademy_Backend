package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** No extiende AuditableEntity: enrollments solo tiene enrolled_at (timestamp de negocio, no de auditoría). */
@Entity
@Table(name = "enrollments")
public class EnrollmentEntity {

    @Id
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    protected EnrollmentEntity() {
        // requerido por JPA
    }

    public EnrollmentEntity(UUID id, UUID courseId, UUID studentId, EnrollmentStatus status, Instant enrolledAt) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.status = status;
        this.enrolledAt = enrolledAt;
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

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }
}
