package io.github.agilesoftware5.systemacademy.assessments.domain.model.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entidad interna del aggregate Assessment: solo se crea/lee a través de Assessment,
 * nunca tiene su propio repositorio. MVP: solo preguntas abiertas (ver decisión de alcance
 * en assessments — preguntas cerradas quedan fuera hasta que el negocio las pida).
 */
public class Question {

    private final UUID id;
    private final int position;
    private final String stem;
    private final String modelAnswer;
    private final String explanation;
    private final List<RubricCriterion> criteria;

    private Question(UUID id, int position, String stem, String modelAnswer, String explanation, List<RubricCriterion> criteria) {
        this.id = id;
        this.position = position;
        this.stem = stem;
        this.modelAnswer = modelAnswer;
        this.explanation = explanation;
        this.criteria = criteria;
    }

    /** Alta de pregunta abierta: exige al menos un criterio, si no el motor de IA no tiene contra qué comparar. */
    public static Question openQuestion(int position, String stem, String modelAnswer, String explanation, List<String> criteriaDescriptions) {
        validateStem(stem);
        validateModelAnswer(modelAnswer);
        if (criteriaDescriptions == null || criteriaDescriptions.isEmpty()) {
            throw new IllegalArgumentException("una pregunta abierta necesita al menos un criterio de evaluación");
        }
        List<RubricCriterion> criteria = new ArrayList<>();
        int criterionPosition = 1;
        for (String description : criteriaDescriptions) {
            criteria.add(RubricCriterion.create(criterionPosition++, description));
        }
        return new Question(UUID.randomUUID(), position, stem, modelAnswer, explanation, criteria);
    }

    public static Question fromPersistence(UUID id, int position, String stem, String modelAnswer, String explanation, List<RubricCriterion> criteria) {
        return new Question(id, position, stem, modelAnswer, explanation, new ArrayList<>(criteria));
    }

    private static void validateStem(String stem) {
        if (stem == null || stem.isBlank()) {
            throw new IllegalArgumentException("el enunciado de la pregunta no puede estar vacío");
        }
    }

    private static void validateModelAnswer(String modelAnswer) {
        if (modelAnswer == null || modelAnswer.isBlank()) {
            throw new IllegalArgumentException("la respuesta modelo no puede estar vacía");
        }
    }

    public UUID getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getStem() {
        return stem;
    }

    public String getModelAnswer() {
        return modelAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<RubricCriterion> getCriteria() {
        return Collections.unmodifiableList(criteria);
    }
}
