package io.github.agilesoftware5.systemacademy.submissions.infrastructure.persistence.adapters;

import io.github.agilesoftware5.systemacademy.submissions.application.internal.grading.AnswerGrader;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.Answer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.entities.CriterionResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.*;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** BE-01: reintentar una respuesta FAILED contra Postgres real, con el unique de criterion_results activo. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(SubmissionRepositoryAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SubmissionRepositoryAdapterRetryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16.4-alpine");

    @Autowired
    SubmissionRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    private final UUID criterionA = UUID.randomUUID();
    private final UUID criterionB = UUID.randomUUID();

    @Test
    void failedAttemptLeavesNoPartialRowsAndRetrySucceeds() {
        var engine = new ScriptedEngine();
        // intento 1: un criterio válido y uno con veredicto nulo -> falla a medias
        engine.enqueue(() -> new GradingResult("x", "m", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionB, null, "e", "f"))));
        engine.enqueue(this::validResult);
        var grader = graderWith(engine);
        var answer = persistedAnswer(GradingStatus.PENDING, List.of());

        var first = grader.grade(reload(answer), answer.getId());
        assertTrue(first.isFailure());
        assertEquals(0, rows(answer.getId()), "un intento fallido no persiste resultados parciales");

        var retry = grader.grade(reload(answer), answer.getId());

        assertTrue(retry.isSuccess(), "el reintento debe responder OK");
        assertEquals(2, rows(answer.getId()));
        assertEquals("COMPLETED", status(answer.getId()));
    }

    /** Filas parciales que dejó el código anterior en una respuesta FAILED: el reintento no debe chocar con el unique. */
    @Test
    void retryReplacesLegacyPartialRowsWithoutViolatingTheUniqueConstraint() {
        var engine = new ScriptedEngine();
        engine.enqueue(this::validResult);
        var grader = graderWith(engine);
        var legacyRow = CriterionResult.create(UUID.randomUUID(), criterionA, CriterionVerdict.MEETS, "e", "f");
        var answer = persistedAnswer(GradingStatus.FAILED, List.of(legacyRow));
        assertEquals(1, rows(answer.getId()));

        var retry = grader.grade(reload(answer), answer.getId());

        assertTrue(retry.isSuccess(), "el reintento debe responder OK");
        assertEquals(2, rows(answer.getId()), "sin filas duplicadas");
        assertEquals("COMPLETED", status(answer.getId()));
    }

    private GradingResult validResult() {
        return new GradingResult("bien", "m", List.of(
                new CriterionGradingResult(criterionA, CriterionVerdict.MEETS, "e", "f"),
                new CriterionGradingResult(criterionB, CriterionVerdict.DOES_NOT_MEET, "e", "f")));
    }

    private AnswerGrader graderWith(GradingEngine engine) {
        return new AnswerGrader(
                repository,
                (assessmentId, questionId, text) -> new QuestionGradingInput(questionId, "stem", "model", text,
                        List.of(new RubricCriterionInput(criterionA, "a"), new RubricCriterionInput(criterionB, "b"))),
                engine);
    }

    private Answer persistedAnswer(GradingStatus status, List<CriterionResult> results) {
        var submissionId = UUID.randomUUID();
        var answerId = UUID.randomUUID();
        var answer = Answer.fromPersistence(answerId, submissionId, UUID.randomUUID(), "respuesta", status,
                null, null, status == GradingStatus.FAILED ? "fallo previo" : null, null, null,
                OffsetDateTime.now(),
                results.stream()
                        .map(r -> CriterionResult.fromPersistence(r.getId(), answerId, r.getRubricCriterionId(),
                                r.getAiVerdict(), r.getAiEvidence(), r.getAiFeedback(),
                                r.getFinalVerdict(), r.getFinalFeedback()))
                        .toList());
        var submission = Submission.fromPersistence(submissionId, UUID.randomUUID(), UUID.randomUUID(),
                SubmissionStatus.SUBMITTED, OffsetDateTime.now(), OffsetDateTime.now(), List.of(answer));
        repository.save(submission);
        return answer;
    }

    private Submission reload(Answer answer) {
        return repository.findById(answer.getSubmissionId()).orElseThrow();
    }

    private String status(UUID answerId) {
        return jdbc.queryForObject("select grading_status from answers where id = ?", String.class, answerId);
    }

    private int rows(UUID answerId) {
        return jdbc.queryForObject("select count(*) from criterion_results where answer_id = ?", Integer.class, answerId);
    }

    private static class ScriptedEngine implements GradingEngine {
        private final Deque<Supplier<GradingResult>> script = new ArrayDeque<>();

        void enqueue(Supplier<GradingResult> step) {
            script.add(step);
        }

        @Override
        public GradingResult grade(QuestionGradingInput input) {
            return script.poll().get();
        }
    }
}
