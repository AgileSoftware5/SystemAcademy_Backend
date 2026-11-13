package io.github.agilesoftware5.systemacademy.submissions.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionOwnershipGuard;
import io.github.agilesoftware5.systemacademy.submissions.application.internal.guards.SubmissionWindowGuard;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.AddAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.StartSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.SubmitSubmissionCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.commands.UpdateAnswerCommand;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.SubmissionStatus;
import io.github.agilesoftware5.systemacademy.submissions.support.FakeAssessmentAccess;
import io.github.agilesoftware5.systemacademy.submissions.support.InMemorySubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas que protegen al alumno y a la evaluación mientras se rinde. */
class StudentSubmissionRulesTest {

    private final UUID assessmentId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();
    private final UUID questionId = UUID.randomUUID();

    private InMemorySubmissionRepository repository;
    private FakeAssessmentAccess assessmentAccess;
    private SubmissionWindowGuard windowGuard;
    private SubmissionOwnershipGuard ownershipGuard;

    @BeforeEach
    void setUp() {
        repository = new InMemorySubmissionRepository();
        assessmentAccess = new FakeAssessmentAccess(UUID.randomUUID());
        assessmentAccess.questionIds.add(questionId);
        windowGuard = new SubmissionWindowGuard(assessmentAccess, Clock.systemUTC());
        ownershipGuard = new SubmissionOwnershipGuard(repository);
    }

    private Submission startedSubmission() {
        return repository.save(Submission.start(assessmentId, studentId));
    }

    @Nested
    class Start {

        private StartSubmissionCommandServiceImpl service;

        @BeforeEach
        void setUp() {
            service = new StartSubmissionCommandServiceImpl(repository, assessmentAccess, windowGuard);
        }

        @Test
        void enrolledStudentStartsAnOpenAssessment() {
            var result = service.handle(new StartSubmissionCommand(assessmentId, studentId));

            assertTrue(result.isSuccess());
            assertEquals(SubmissionStatus.IN_PROGRESS, result.getValue().getStatus());
        }

        @Test
        void studentOutsideTheCourseIsForbidden() {
            assessmentAccess.enrolled = false;

            var result = service.handle(new StartSubmissionCommand(assessmentId, studentId));

            assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        }

        @Test
        void closedOrDraftAssessmentCannotBeStarted() {
            assessmentAccess.open = false;

            var result = service.handle(new StartSubmissionCommand(assessmentId, studentId));

            assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        }

        @Test
        void studentCannotStartASecondAttempt() {
            startedSubmission();

            var result = service.handle(new StartSubmissionCommand(assessmentId, studentId));

            assertEquals(ApplicationErrorCode.CONFLICT, result.getError().code());
        }
    }

    @Nested
    class Answer {

        private AddAnswerCommandServiceImpl addService;
        private UpdateAnswerCommandServiceImpl updateService;

        @BeforeEach
        void setUp() {
            addService = new AddAnswerCommandServiceImpl(repository, ownershipGuard, windowGuard, assessmentAccess);
            updateService = new UpdateAnswerCommandServiceImpl(repository, ownershipGuard, windowGuard);
        }

        @Test
        void answersAQuestionOfTheAssessment() {
            var submission = startedSubmission();

            var result = addService.handle(new AddAnswerCommand(submission.getId(), studentId, questionId, "texto"));

            assertTrue(result.isSuccess());
        }

        @Test
        void questionFromAnotherAssessmentIsRejected() {
            var submission = startedSubmission();

            var result = addService.handle(
                    new AddAnswerCommand(submission.getId(), studentId, UUID.randomUUID(), "texto"));

            assertEquals(ApplicationErrorCode.VALIDATION_ERROR, result.getError().code());
        }

        @Test
        void cannotAnswerOnceTheAssessmentClosed() {
            var submission = startedSubmission();
            assessmentAccess.open = false;

            var result = addService.handle(new AddAnswerCommand(submission.getId(), studentId, questionId, "texto"));

            assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        }

        @Test
        void cannotEditAnswersOnceTheAssessmentClosed() {
            var submission = startedSubmission();
            var added = addService.handle(new AddAnswerCommand(submission.getId(), studentId, questionId, "texto"));
            UUID answerId = added.getValue().getAnswers().get(0).getId();
            assessmentAccess.open = false;

            var result = updateService.handle(
                    new UpdateAnswerCommand(submission.getId(), studentId, answerId, "otro texto"));

            assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        }

        @Test
        void anotherStudentCannotTouchTheSubmission() {
            var submission = startedSubmission();

            var result = addService.handle(
                    new AddAnswerCommand(submission.getId(), UUID.randomUUID(), questionId, "texto"));

            assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        }
    }

    @Nested
    class Submit {

        @Test
        void cannotSubmitOnceTheAssessmentClosed() {
            var service = new SubmitSubmissionCommandServiceImpl(repository, ownershipGuard, windowGuard);
            var submission = startedSubmission();
            assessmentAccess.open = false;

            var result = service.handle(new SubmitSubmissionCommand(submission.getId(), studentId));

            assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
            assertEquals(SubmissionStatus.IN_PROGRESS, submission.getStatus());
        }
    }
}
