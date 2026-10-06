package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.OverrideCriterionResultCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.support.FakeAssessmentAccess;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.support.SubmissionFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OverrideCriterionResultCommandServiceImplTest {

    private final UUID teacherId = UUID.randomUUID();
    private OverrideCriterionResultCommandServiceImpl service;
    private Submission submission;
    private Answer answer;
    private CriterionResult criterionResult;

    @BeforeEach
    void setUp() {
        var repository = new InMemorySubmissionRepository();
        service = new OverrideCriterionResultCommandServiceImpl(repository, new FakeAssessmentAccess(teacherId));
        submission = repository.save(SubmissionFixtures.gradedSubmission());
        answer = submission.getAnswers().get(0);
        criterionResult = answer.getCriterionResults().get(0);
    }

    private OverrideCriterionResultCommand commandBy(UUID reviewerId) {
        return new OverrideCriterionResultCommand(submission.getId(), answer.getId(), criterionResult.getId(),
                CriterionVerdict.PARTIALLY_MEETS, "le faltó un ejemplo", reviewerId);
    }

    @Test
    void teacherOverridesVerdictAndIsRecordedAsReviewer() {
        var result = service.handle(commandBy(teacherId));

        assertTrue(result.isSuccess());
        assertEquals(CriterionVerdict.PARTIALLY_MEETS, criterionResult.getFinalVerdict());
        assertEquals(CriterionVerdict.MEETS, criterionResult.getAiVerdict());
        assertEquals(teacherId, answer.getReviewedBy());
        assertNotNull(answer.getReviewedAt());
    }

    @Test
    void teacherOutsideTheCourseCannotOverride() {
        var result = service.handle(commandBy(UUID.randomUUID()));

        assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        assertEquals(CriterionVerdict.MEETS, criterionResult.getFinalVerdict());
        assertNull(answer.getReviewedBy());
    }
}
