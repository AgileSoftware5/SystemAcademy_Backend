package io.github.agilesoftware5.systemacademy.submissions.application.internal.eventhandlers;

import io.github.agilesoftware5.systemacademy.submissions.application.internal.grading.AnswerGrader;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.events.SubmissionSubmittedEvent;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.*;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionSubmittedEventHandlerTest {

    private final UUID criterionId = UUID.randomUUID();
    private final InMemorySubmissionRepository repository = new InMemorySubmissionRepository();
    private final AtomicInteger engineCalls = new AtomicInteger();

    private SubmissionSubmittedEventHandler handlerWithEngineFailingOn(int failingCall) {
        var grader = new AnswerGrader(
                repository,
                (assessmentId, questionId, answerText) -> new QuestionGradingInput(questionId, "stem", null,
                        answerText, List.of(new RubricCriterionInput(criterionId, "criterio"))),
                input -> {
                    if (engineCalls.incrementAndGet() == failingCall) {
                        throw new RuntimeException("cuota agotada");
                    }
                    return new GradingResult("ok", "test-model", List.of(
                            new CriterionGradingResult(criterionId, CriterionVerdict.MEETS, "e", "f")));
                });
        return new SubmissionSubmittedEventHandler(repository, grader);
    }

    private Submission submittedWithAnswers(int count) {
        Submission submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        for (int i = 0; i < count; i++) {
            submission.addAnswer(Answer.create(submission.getId(), UUID.randomUUID(), "respuesta " + i));
        }
        submission.submit();
        return repository.save(submission);
    }

    @Test
    void gradesEveryAnswerOfTheSubmittedSubmission() {
        Submission submission = submittedWithAnswers(3);

        handlerWithEngineFailingOn(-1).on(new SubmissionSubmittedEvent(submission.getId(), submission.getAssessmentId()));

        assertEquals(3, engineCalls.get());
        assertTrue(submission.getAnswers().stream()
                .allMatch(a -> a.getGradingStatus() == GradingStatus.COMPLETED));
    }

    @Test
    void oneFailedAnswerDoesNotStopTheOthers() {
        Submission submission = submittedWithAnswers(3);

        handlerWithEngineFailingOn(2).on(new SubmissionSubmittedEvent(submission.getId(), submission.getAssessmentId()));

        assertEquals(3, engineCalls.get());
        assertEquals(List.of(GradingStatus.COMPLETED, GradingStatus.FAILED, GradingStatus.COMPLETED),
                submission.getAnswers().stream().map(Answer::getGradingStatus).toList());
    }

    @Test
    void missingSubmissionIsIgnored() {
        assertDoesNotThrow(() -> handlerWithEngineFailingOn(-1)
                .on(new SubmissionSubmittedEvent(UUID.randomUUID(), UUID.randomUUID())));
        assertEquals(0, engineCalls.get());
    }
}
