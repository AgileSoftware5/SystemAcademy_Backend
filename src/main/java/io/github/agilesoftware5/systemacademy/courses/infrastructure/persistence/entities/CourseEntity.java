package io.github.agilesoftware5.systemacademy.courses.infrastructure.persistence.entities;

import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.CourseStatus;
import io.github.agilesoftware5.systemacademy.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "courses")
public class CourseEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "enrollment_code", nullable = false, unique = true)
    private String enrollmentCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;

    protected CourseEntity() {
        // requerido por JPA
    }

    public CourseEntity(UUID id, UUID instructorId, UUID createdBy, String code, String name,
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
