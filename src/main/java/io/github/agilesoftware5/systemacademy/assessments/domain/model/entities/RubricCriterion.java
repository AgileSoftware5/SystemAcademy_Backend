package io.github.agilesoftware5.systemacademy.assessments.domain.model.entities;

import java.util.UUID;

/** Entidad interna del aggregate Assessment: solo se accede a través de una Question. */
public class RubricCriterion {

    private final UUID id;
    private final int position;
    private final String description;

    private RubricCriterion(UUID id, int position, String description) {
        this.id = id;
        this.position = position;
        this.description = description;
    }

    public static RubricCriterion create(int position, String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("la descripción del criterio no puede estar vacía");
        }
        return new RubricCriterion(UUID.randomUUID(), position, description);
    }

    public static RubricCriterion fromPersistence(UUID id, int position, String description) {
        return new RubricCriterion(id, position, description);
    }

    public UUID getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }
}
