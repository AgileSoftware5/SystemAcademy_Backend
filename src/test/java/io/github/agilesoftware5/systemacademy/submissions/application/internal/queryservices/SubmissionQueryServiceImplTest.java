package io.github.agilesoftware5.systemacademy.submissions.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.AreResultsReleasedQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByAssessmentQuery;
import io.github.agilesoftware5.systemacademy.submissions.support.FakeAssessmentAccess;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.support.SubmissionFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionQueryServiceImplTest {

    private final UUID teacherId = UUID.randomUUID();
    private FakeAssessmentAccess assessmentAccess;
    private SubmissionQueryServiceImpl service;
    private Submission submission;

    @BeforeEach
    void setUp() {
        var repository = new InMemorySubmissionRepository();
        assessmentAccess = new FakeAssessmentAccess(teacherId);
        service = new SubmissionQueryServiceImpl(repository, assessmentAccess);
        submission = repository.save(SubmissionFixtures.gradedSubmission());
    }

    @Test
    void courseTeacherListsSubmissionsOfTheAssessment() {
        var submissions = service.handle(new ListSubmissionsByAssessmentQuery(submission.getAssessmentId(), teacherId));

        assertEquals(1, submissions.size());
    }

    @Test
    void teacherOutsideTheCourseGetsNothing() {
        var submissions = service.handle(
                new ListSubmissionsByAssessmentQuery(submission.getAssessmentId(), UUID.randomUUID()));

        assertTrue(submissions.isEmpty());
    }

    @Test
    void reportsWhetherResultsWereReleased() {
        assertFalse(service.handle(new AreResultsReleasedQuery(submission.getAssessmentId())));

        assessmentAccess.resultsReleased = true;

        assertTrue(service.handle(new AreResultsReleasedQuery(submission.getAssessmentId())));
    }
}
