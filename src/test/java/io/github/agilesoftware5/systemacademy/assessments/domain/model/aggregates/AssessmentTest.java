package io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates;

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
}
