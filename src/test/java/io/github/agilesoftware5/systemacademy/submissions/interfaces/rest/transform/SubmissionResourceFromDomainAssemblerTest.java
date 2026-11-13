package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.support.SubmissionFixtures;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionResourceFromDomainAssemblerTest {

    private Submission reviewedSubmission() {
        Submission submission = SubmissionFixtures.gradedSubmission();
        var answer = submission.getAnswers().get(0);
        submission.overrideCriterionResult(answer.getId(), answer.getCriterionResults().get(0).getId(),
                CriterionVerdict.DOES_NOT_MEET, "corregido por el docente", UUID.randomUUID());
        return submission;
    }

    @Test
    void teacherSeesEverything() {
        var answer = SubmissionResourceFromDomainAssembler
                .toResource(reviewedSubmission(), GradingVisibility.FULL).answers().get(0);

        assertEquals("test-model", answer.aiModel());
        assertNotNull(answer.reviewedBy());
        assertEquals(CriterionVerdict.MEETS, answer.criterionResults().get(0).aiVerdict());
        assertEquals(CriterionVerdict.DOES_NOT_MEET, answer.criterionResults().get(0).finalVerdict());
    }

    @Test
    void studentSeesNoGradingBeforeRelease() {
        var answer = SubmissionResourceFromDomainAssembler
                .toResource(reviewedSubmission(), GradingVisibility.HIDDEN).answers().get(0);

        assertEquals("oculta el estado interno", answer.answerText());
        assertNull(answer.gradingStatus());
        assertNull(answer.aiGeneralFeedback());
        assertTrue(answer.criterionResults().isEmpty());
    }

    @Test
    void studentSeesOnlyTheFinalVerdictAfterRelease() {
        var answer = SubmissionResourceFromDomainAssembler
                .toResource(reviewedSubmission(), GradingVisibility.RELEASED).answers().get(0);
        var criterion = answer.criterionResults().get(0);

        assertEquals("feedback general", answer.aiGeneralFeedback());
        assertEquals(CriterionVerdict.DOES_NOT_MEET, criterion.finalVerdict());
        assertEquals("corregido por el docente", criterion.finalFeedback());
        assertNull(criterion.aiVerdict());
        assertNull(criterion.aiFeedback());
        assertNull(answer.aiModel());
        assertNull(answer.errorMessage());
        assertNull(answer.reviewedBy());
    }
}
