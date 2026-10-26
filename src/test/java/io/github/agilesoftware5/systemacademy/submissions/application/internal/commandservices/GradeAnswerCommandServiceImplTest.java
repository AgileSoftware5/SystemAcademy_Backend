package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.grading.AnswerGrader;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.GradeAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.*;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngineException;
import io.github.agilesoftware5.systemacademy.submissions.support.FakeAssessmentAccess;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GradeAnswerCommandServiceImplTest {

    private final UUID criterionA = UUID.randomUUID();
    private final UUID criterionB = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    private InMemorySubmissionRepository repository;
    private ScriptedGradingEngine engine;
    private GradeAnswerCommandServiceImpl service;
    private Submission submission;
    private Answer answer;

    @BeforeEach
    void setUp() {
        repository = new InMemorySubmissionRepository();
        engine = new ScriptedGradingEngine();
        var grader = new AnswerGrader(
                repository,
                (assessmentId, questionId, answerText) -> new QuestionGradingInput(
                        questionId, "¿Qué es la encapsulación?", "respuesta modelo", answerText,
                        List.of(new RubricCriterionInput(criterionA, "oculta estado"),
                                new RubricCriterionInput(criterionB, "expone métodos"))),
                engine);
        service = new GradeAnswerCommandServiceImpl(repository, new FakeAssessmentAccess(teacherId), grader);

        submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        answer = Answer.create(submission.getId(), UUID.randomUUID(), "oculta el estado interno");
        submission.addAnswer(answer);
        submission.submit();
        repository.save(submission);
    }

    private GradeAnswerCommand command() {
        return new GradeAnswerCommand(submission.getId(), answer.getId(), teacherId);
    }

    private GradingResult validResult() {
        return new GradingResult("bien", "test-model", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionB, CriterionVerdict.DOES_NOT_MEET, "e", "f")));
    }

    @Test
    void engineFailureMarksAnswerAsFailedAndPersistsIt() {
        engine.enqueue(() -> { throw new GradingEngineException("el servicio de IA tardó demasiado en responder"); });

        var result = service.handle(command());

        assertTrue(result.isFailure());
        assertEquals(GradingStatus.FAILED, answer.getGradingStatus());
        assertEquals("el servicio de IA tardó demasiado en responder", answer.getErrorMessage());
        assertEquals(2, repository.saves);
    }

    @Test
    void failedAnswerCanBeGradedAgainUntilCompleted() {
        engine.enqueue(() -> { throw new GradingEngineException("el servicio de IA no está disponible"); });
        engine.enqueue(this::validResult);

        service.handle(command());
        var retry = service.handle(command());

        assertTrue(retry.isSuccess());
        assertEquals(GradingStatus.COMPLETED, answer.getGradingStatus());
        assertNull(answer.getErrorMessage());
        assertEquals(2, answer.getCriterionResults().size());
    }

    @Test
    void partialResultsFromFailedAttemptAreReplacedOnRetry() {
        engine.enqueue(() -> new GradingResult("x", "test-model", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionB, null, "e", "f"))));
        engine.enqueue(this::validResult);

        service.handle(command());
        assertEquals(GradingStatus.FAILED, answer.getGradingStatus());
        assertEquals(0, answer.getCriterionResults().size(), "un intento fallido no deja resultados parciales");

        var retry = service.handle(command());

        assertTrue(retry.isSuccess());
        assertEquals(2, answer.getCriterionResults().size());
        assertEquals(Set.of(criterionA, criterionB), answer.getCriterionResults().stream()
                .map(r -> r.getRubricCriterionId()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void inconsistentEngineResultMarksAnswerAsFailed() {
        engine.enqueue(() -> new GradingResult("x", "test-model", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"))));

        var result = service.handle(command());

        assertTrue(result.isFailure());
        assertEquals(GradingStatus.FAILED, answer.getGradingStatus());
        assertEquals(2, repository.saves);
    }

    @Test
    void teacherOutsideTheCourseCannotTriggerGrading() {
        var result = service.handle(new GradeAnswerCommand(submission.getId(), answer.getId(), UUID.randomUUID()));

        assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        assertEquals(0, engine.calls);
        assertEquals(GradingStatus.PENDING, answer.getGradingStatus());
    }

    @Test
    void criterionResultsFollowTheRubricOrderNotTheEngineOrder() {
        engine.enqueue(() -> new GradingResult("bien", "test-model", List.of(
                new CriterionGradingResult(criterionB, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"))));

        service.handle(command());

        assertEquals(List.of(criterionA, criterionB), answer.getCriterionResults().stream()
                .map(r -> r.getRubricCriterionId()).toList());
    }

    @Test
    void completedAnswerIsNotGradedTwice() {
        engine.enqueue(this::validResult);
        service.handle(command());

        var second = service.handle(command());

        assertTrue(second.isFailure());
        assertEquals(1, engine.calls);
    }

    private static class ScriptedGradingEngine implements GradingEngine {
        private final Deque<Supplier<GradingResult>> script = new ArrayDeque<>();
        private int calls;

        void enqueue(Supplier<GradingResult> step) {
            script.add(step);
        }

        @Override
        public GradingResult grade(QuestionGradingInput input) {
            calls++;
            return script.poll().get();
        }
    }
}
