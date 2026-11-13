package io.github.agilesoftware5.systemacademy.submissions.application.internal.grading;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.*;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngineException;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class AnswerGraderTest {

    private final UUID criterionA = UUID.randomUUID();
    private final UUID criterionB = UUID.randomUUID();

    private FlakySaveRepository repository;
    private Deque<Supplier<GradingResult>> script;
    private AnswerGrader grader;
    private Submission submission;
    private Answer answer;

    @BeforeEach
    void setUp() {
        repository = new FlakySaveRepository();
        script = new ArrayDeque<>();
        GradingEngine engine = input -> script.poll().get();
        grader = new AnswerGrader(
                repository,
                (assessmentId, questionId, text) -> new QuestionGradingInput(questionId, "stem", "model", text,
                        List.of(new RubricCriterionInput(criterionA, "a"), new RubricCriterionInput(criterionB, "b"))),
                engine);

        submission = Submission.start(UUID.randomUUID(), UUID.randomUUID());
        answer = Answer.create(submission.getId(), UUID.randomUUID(), "respuesta");
        submission.addAnswer(answer);
        submission.submit();
        repository.save(submission);
        repository.saves = 0;
    }

    private GradingResult validResult() {
        return new GradingResult("bien", "m", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionB, CriterionVerdict.MEETS, "e", "f")));
    }

    @Test
    void engineExceptionMessageIsStoredForTheTeacher() {
        script.add(() -> { throw new GradingEngineException("el servicio de IA está saturado"); });

        var result = grader.grade(submission, answer.getId());

        assertEquals("el servicio de IA está saturado", result.getError().message());
        assertEquals("el servicio de IA está saturado", answer.getErrorMessage());
    }

    @Test
    void technicalExceptionDetailIsNotExposed() {
        script.add(() -> { throw new RuntimeException("I/O error on POST request for \"http://ai:8000/api/v1/grade\""); });

        var result = grader.grade(submission, answer.getId());

        assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        assertEquals("error inesperado durante la evaluación", answer.getErrorMessage());
    }

    @Test
    void saveFailureAfterGradingLeavesPersistedAnswerFailedAndRetryable() {
        // La BD conserva la respuesta PENDING; la copia en memoria quedará COMPLETED cuando falle el guardado.
        repository.persisted = copyOf(submission);
        repository.failNextSave = true;
        script.add(this::validResult);

        var result = grader.grade(submission, answer.getId());

        assertTrue(result.isFailure(), "debe devolver un fallo controlado, no lanzar");
        var persistedAnswer = repository.persisted.getAnswers().get(0);
        assertEquals(GradingStatus.FAILED, persistedAnswer.getGradingStatus());
        assertTrue(persistedAnswer.getCriterionResults().isEmpty());
        assertNotNull(persistedAnswer.getErrorMessage());

        script.add(this::validResult);
        var retry = grader.grade(repository.persisted, answer.getId());

        assertTrue(retry.isSuccess());
        assertEquals(GradingStatus.COMPLETED, repository.persisted.getAnswers().get(0).getGradingStatus());
    }

    @Test
    void failureWhileRecordingTheFailureDoesNotEscape() {
        repository.persisted = copyOf(submission);
        repository.failAllSaves = true;
        script.add(this::validResult);

        var result = assertDoesNotThrow(() -> grader.grade(submission, answer.getId()));

        assertTrue(result.isFailure());
    }

    private Submission copyOf(Submission source) {
        var original = source.getAnswers().get(0);
        var copy = Answer.fromPersistence(original.getId(), source.getId(), original.getQuestionId(),
                original.getAnswerText(), GradingStatus.PENDING, null, null, null, null, null,
                original.getUpdatedAt(), List.of());
        return Submission.fromPersistence(source.getId(), source.getAssessmentId(), source.getStudentId(),
                source.getStatus(), source.getStartedAt(), source.getSubmittedAt(), List.of(copy));
    }

    /** Simula un repositorio cuyo guardado falla; findById devuelve lo "persistido", no la copia en memoria. */
    private static class FlakySaveRepository extends InMemorySubmissionRepository {
        Submission persisted;
        boolean failNextSave;
        boolean failAllSaves;

        @Override
        public Submission save(Submission submission) {
            if (failAllSaves || failNextSave) {
                failNextSave = false;
                throw new RuntimeException("duplicate key value violates unique constraint");
            }
            if (persisted != null) {
                persisted = submission;
                saves++;
                return submission;
            }
            return super.save(submission);
        }

        @Override
        public Optional<Submission> findById(UUID id) {
            return persisted != null ? Optional.of(persisted) : super.findById(id);
        }
    }
}
