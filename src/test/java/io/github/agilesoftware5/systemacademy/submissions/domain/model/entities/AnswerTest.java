package io.github.agilesoftware5.systemacademy.submissions.domain.model.entities;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {

    private final UUID criterionId = UUID.randomUUID();

    private Answer newAnswer() {
        return Answer.create(UUID.randomUUID(), UUID.randomUUID(), "respuesta del alumno");
    }

    private Answer failedAnswerWithPartialResult() {
        Answer answer = newAnswer();
        answer.startGrading();
        answer.addCriterionResult(CriterionResult.create(
                answer.getId(), criterionId, CriterionVerdict.MEETS, "evidencia", "feedback"));
        answer.markGradingError("timeout del servicio de IA");
        return answer;
    }

    @Test
    void failedAnswerCanStartGradingAgain() {
        Answer answer = failedAnswerWithPartialResult();

        answer.startGrading();

        assertEquals(GradingStatus.PROCESSING, answer.getGradingStatus());
        assertNull(answer.getErrorMessage());
        assertTrue(answer.getCriterionResults().isEmpty());
    }

    @Test
    void retryAfterFailureAcceptsTheSameCriteriaAgain() {
        Answer answer = failedAnswerWithPartialResult();

        answer.startGrading();
        answer.addCriterionResult(CriterionResult.create(
                answer.getId(), criterionId, CriterionVerdict.PARTIALLY_MEETS, "evidencia", "feedback"));
        answer.completeGrading("feedback general", "modelo");

        assertEquals(GradingStatus.COMPLETED, answer.getGradingStatus());
        assertEquals(1, answer.getCriterionResults().size());
        assertEquals(CriterionVerdict.PARTIALLY_MEETS, answer.getCriterionResults().get(0).getAiVerdict());
    }

    @Test
    void completedAnswerCannotStartGradingAgain() {
        Answer answer = newAnswer();
        answer.startGrading();
        answer.completeGrading("feedback general", "modelo");

        assertThrows(IllegalStateException.class, answer::startGrading);
    }

    @Test
    void answerInProcessingCannotStartGradingAgain() {
        Answer answer = newAnswer();
        answer.startGrading();

        assertThrows(IllegalStateException.class, answer::startGrading);
    }
}
