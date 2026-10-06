package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.ReleaseAssessmentResultsCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.CourseStatus;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseAssessmentResultsCommandServiceImplTest {

    private final UUID teacherId = UUID.randomUUID();
    private final Map<UUID, Assessment> store = new HashMap<>();
    private long ungradedAnswers;
    private ReleaseAssessmentResultsCommandServiceImpl service;
    private Assessment assessment;

    @BeforeEach
    void setUp() {
        Course course = Course.fromPersistence(UUID.randomUUID(), teacherId, teacherId,
                "POO-101", "POO", null, "ABC123", CourseStatus.ACTIVE);
        service = new ReleaseAssessmentResultsCommandServiceImpl(
                new InMemoryAssessmentRepository(),
                new CourseOwnershipGuard(new SingleCourseQueryService(course)),
                assessmentId -> ungradedAnswers);

        Instant opensAt = Instant.now().minus(Duration.ofHours(3));
        assessment = Assessment.create(course.getId(), "Parcial", null);
        assessment.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));
        assessment.publish(opensAt, opensAt.plus(Duration.ofHours(1)));
        store.put(assessment.getId(), assessment);
    }

    @Test
    void releasesResultsWhenEveryAnswerIsGraded() {
        var result = service.handle(new ReleaseAssessmentResultsCommand(assessment.getId(), teacherId));

        assertTrue(result.isSuccess());
        assertTrue(store.get(assessment.getId()).areResultsReleased());
    }

    @Test
    void refusesToReleaseWhileAnswersAreUngraded() {
        ungradedAnswers = 3;

        var result = service.handle(new ReleaseAssessmentResultsCommand(assessment.getId(), teacherId));

        assertTrue(result.isFailure());
        assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        assertTrue(result.getError().message().contains("3"));
        assertFalse(assessment.areResultsReleased());
    }

    @Test
    void onlyTheCourseTeacherCanReleaseResults() {
        var result = service.handle(new ReleaseAssessmentResultsCommand(assessment.getId(), UUID.randomUUID()));

        assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        assertFalse(assessment.areResultsReleased());
    }

    @Test
    void unknownAssessmentIsNotFound() {
        var result = service.handle(new ReleaseAssessmentResultsCommand(UUID.randomUUID(), teacherId));

        assertEquals(ApplicationErrorCode.NOT_FOUND, result.getError().code());
    }

    private class InMemoryAssessmentRepository implements AssessmentRepository {
        @Override
        public Assessment save(Assessment assessment) {
            store.put(assessment.getId(), assessment);
            return assessment;
        }

        @Override
        public Optional<Assessment> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Assessment> findByCourseId(UUID courseId) {
            return store.values().stream().filter(a -> a.getCourseId().equals(courseId)).toList();
        }
    }

    private record SingleCourseQueryService(Course course) implements CourseQueryService {
        @Override
        public Optional<Course> handle(GetCourseByIdQuery query) {
            return Optional.of(course).filter(c -> c.getId().equals(query.courseId()));
        }

        @Override
        public List<Course> handle(ListCoursesByInstructorQuery query) {
            return List.of(course);
        }
    }
}
