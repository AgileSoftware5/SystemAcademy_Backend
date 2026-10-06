package io.github.agilesoftware5.systemacademy.submissions.support;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;

import java.util.UUID;

public final class SubmissionFixtures {

    private SubmissionFixtures() {
    }

    public static Submission submittedWithOneAnswer() {
        Submission submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        submission.addAnswer(Answer.create(submission.getId(), UUID.randomUUID(), "oculta el estado interno"));
        submission.submit();
        return submission;
    }

    /** Entrega enviada con una respuesta ya calificada por la IA (un criterio, MEETS). */
    public static Submission gradedSubmission() {
        Submission submission = submittedWithOneAnswer();
        Answer answer = submission.getAnswers().get(0);
        answer.startGrading();
        answer.addCriterionResult(CriterionResult.create(
                answer.getId(), UUID.randomUUID(), CriterionVerdict.MEETS, "evidencia", "feedback IA"));
        answer.completeGrading("feedback general", "test-model");
        return submission;
    }
}
