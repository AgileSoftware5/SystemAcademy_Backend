package io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.valueobjects.AssessmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentTest {

    private final Instant opensAt = Instant.parse("2026-10-01T10:00:00Z");
    private final Instant closesAt = opensAt.plus(Duration.ofHours(2));

    private Assessment publishedAssessment() {
        Assessment assessment = Assessment.create(UUID.randomUUID(), "Parcial", null);
        assessment.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));
        assessment.publish(opensAt, closesAt);
        return assessment;
    }

    @Test
    void resultsCanBeReleasedOnceTheAssessmentHasClosed() {
        Assessment assessment = publishedAssessment();
        Instant now = closesAt.plusSeconds(1);

        assessment.releaseResults(now);

        assertTrue(assessment.areResultsReleased());
        assertEquals(now, assessment.getResultsReleasedAt());
    }

    @Test
    void resultsCannotBeReleasedWhileStudentsCanStillSubmit() {
        Assessment assessment = publishedAssessment();

        assertThrows(IllegalStateException.class, () -> assessment.releaseResults(closesAt.minusSeconds(1)));
        assertFalse(assessment.areResultsReleased());
    }

    @Test
    void draftAssessmentCannotReleaseResults() {
        Assessment draft = Assessment.create(UUID.randomUUID(), "Parcial", null);

        assertThrows(IllegalStateException.class, () -> draft.releaseResults(Instant.now()));
    }

    @Test
    void resultsCannotBeReleasedTwice() {
        Assessment assessment = publishedAssessment();
        assessment.releaseResults(closesAt.plusSeconds(1));

        assertThrows(IllegalStateException.class, () -> assessment.releaseResults(closesAt.plusSeconds(60)));
    }

    // --- BE-05: edición de borrador ---

    private Assessment draftAssessment() {
        return Assessment.create(UUID.randomUUID(), "Parcial", null);
    }

    @Test
    void draftCanBeEditedAndKeepsTheNewValues() {
        Assessment assessment = draftAssessment();

        assessment.updateDraft("Parcial 1", "Sin apuntes", opensAt, closesAt);

        assertEquals("Parcial 1", assessment.getTitle());
        assertEquals("Sin apuntes", assessment.getInstructions());
        assertEquals(opensAt, assessment.getOpensAt());
        assertEquals(closesAt, assessment.getClosesAt());
        assertEquals(AssessmentStatus.DRAFT, assessment.getStatus());
    }

    @Test
    void draftCanBeEditedWithoutDates() {
        Assessment assessment = draftAssessment();

        assertDoesNotThrow(() -> assessment.updateDraft("Parcial 1", null, null, null));
    }

    @Test
    void publishedAssessmentRejectsEdition() {
        Assessment assessment = publishedAssessment();

        assertThrows(IllegalStateException.class,
                () -> assessment.updateDraft("Otro título", null, opensAt, closesAt));
        assertEquals("Parcial", assessment.getTitle());
    }

    @Test
    void editionRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> draftAssessment().updateDraft(" ", null, null, null));
    }

    @Test
    void editionRejectsClosingNotAfterOpening() {
        Assessment assessment = draftAssessment();

        assertThrows(IllegalArgumentException.class, () -> assessment.updateDraft("Parcial", null, closesAt, opensAt));
        assertThrows(IllegalArgumentException.class, () -> assessment.updateDraft("Parcial", null, opensAt, opensAt));
    }

    @Test
    void failedEditionLeavesTheDraftUntouched() {
        Assessment assessment = draftAssessment();

        assertThrows(IllegalArgumentException.class, () -> assessment.updateDraft("Nuevo", null, closesAt, opensAt));

        assertEquals("Parcial", assessment.getTitle());
        assertNull(assessment.getOpensAt());
    }

    @Test
    void publishUsesTheDatesSavedInTheDraft() {
        Assessment assessment = draftAssessment();
        assessment.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));
        assessment.updateDraft("Parcial", null, opensAt, closesAt);

        assessment.publish(null, null);

        assertEquals(AssessmentStatus.PUBLISHED, assessment.getStatus());
        assertEquals(opensAt, assessment.getOpensAt());
        assertEquals(closesAt, assessment.getClosesAt());
    }

    @Test
    void publishDatesOverrideTheDraftOnes() {
        Assessment assessment = draftAssessment();
        assessment.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));
        assessment.updateDraft("Parcial", null, opensAt, closesAt);
        Instant newClosesAt = closesAt.plus(Duration.ofDays(1));

        assessment.publish(null, newClosesAt);

        assertEquals(opensAt, assessment.getOpensAt());
        assertEquals(newClosesAt, assessment.getClosesAt());
    }

    @Test
    void publishFailsWhenNoDatesWereEverDefined() {
        Assessment assessment = draftAssessment();
        assessment.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));

        assertThrows(IllegalArgumentException.class, () -> assessment.publish(null, null));
        assertEquals(AssessmentStatus.DRAFT, assessment.getStatus());
    }
}
