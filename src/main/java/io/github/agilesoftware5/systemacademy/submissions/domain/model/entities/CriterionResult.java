package io.github.agilesoftware5.systemacademy.submissions.domain.model.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;

import java.util.UUID;

public class CriterionResult {

    private final UUID id;
    private final UUID answerId;
    private final UUID rubricCriterionId;

    private final CriterionVerdict aiVerdict;
    private final String aiEvidence;
    private final String aiFeedback;

    private CriterionVerdict finalVerdict;
    private String finalFeedback;

    private CriterionResult(
            UUID id,
            UUID answerId,
            UUID rubricCriterionId,
            CriterionVerdict aiVerdict,
            String aiEvidence,
            String aiFeedback,
            CriterionVerdict finalVerdict,
            String finalFeedback
    ) {
        this.id = id;
        this.answerId = answerId;
        this.rubricCriterionId = rubricCriterionId;
        this.aiVerdict = aiVerdict;
        this.aiEvidence = aiEvidence;
        this.aiFeedback = aiFeedback;
        this.finalVerdict = finalVerdict;
        this.finalFeedback = finalFeedback;
    }

    public static CriterionResult create(
            UUID answerId,
            UUID rubricCriterionId,
            CriterionVerdict aiVerdict,
            String aiEvidence,
            String aiFeedback
    ) {
        validateId(
                answerId,
                "el id de la respuesta no puede ser nulo"
        );

        validateId(
                rubricCriterionId,
                "el id del criterio de rúbrica no puede ser nulo"
        );

        if (aiVerdict == null) {
            throw new IllegalArgumentException(
                    "el veredicto de la IA no puede ser nulo"
            );
        }

        return new CriterionResult(
                UUID.randomUUID(),
                answerId,
                rubricCriterionId,
                aiVerdict,
                aiEvidence,
                aiFeedback,
                aiVerdict,
                aiFeedback
        );
    }

    public static CriterionResult fromPersistence(
            UUID id,
            UUID answerId,
            UUID rubricCriterionId,
            CriterionVerdict aiVerdict,
            String aiEvidence,
            String aiFeedback,
            CriterionVerdict finalVerdict,
            String finalFeedback
    ) {
        validateId(
                id,
                "el id del resultado no puede ser nulo"
        );

        validateId(
                answerId,
                "el id de la respuesta no puede ser nulo"
        );

        validateId(
                rubricCriterionId,
                "el id del criterio de rúbrica no puede ser nulo"
        );

        if (aiVerdict == null) {
            throw new IllegalArgumentException(
                    "el veredicto de la IA no puede ser nulo"
            );
        }

        if (finalVerdict == null) {
            throw new IllegalArgumentException(
                    "el veredicto final no puede ser nulo"
            );
        }

        return new CriterionResult(
                id,
                answerId,
                rubricCriterionId,
                aiVerdict,
                aiEvidence,
                aiFeedback,
                finalVerdict,
                finalFeedback
        );
    }

    public void overrideResult(
            CriterionVerdict finalVerdict,
            String finalFeedback
    ) {
        if (finalVerdict == null) {
            throw new IllegalArgumentException(
                    "el veredicto final no puede ser nulo"
            );
        }

        this.finalVerdict = finalVerdict;
        this.finalFeedback = finalFeedback;
    }

    private static void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getAnswerId() {
        return answerId;
    }

    public UUID getRubricCriterionId() {
        return rubricCriterionId;
    }

    public CriterionVerdict getAiVerdict() {
        return aiVerdict;
    }

    public String getAiEvidence() {
        return aiEvidence;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public CriterionVerdict getFinalVerdict() {
        return finalVerdict;
    }

    public String getFinalFeedback() {
        return finalFeedback;
    }
}