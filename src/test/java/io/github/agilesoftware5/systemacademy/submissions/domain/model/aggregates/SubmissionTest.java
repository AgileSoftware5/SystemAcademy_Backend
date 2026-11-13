package io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.events.SubmissionSubmittedEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    @Test
    void submittingRegistersAnEventForGrading() {
        Submission submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());

        submission.submit();

        assertEquals(
                List.of(new SubmissionSubmittedEvent(submission.getId(), submission.getAssessmentId())),
                submission.domainEvents()
        );
    }

    @Test
    void eventsAreClearedOncePublished() {
        Submission submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        submission.submit();

        submission.clearDomainEvents();

        assertTrue(submission.domainEvents().isEmpty());
    }
}
