package io.github.agilesoftware5.systemacademy.assessments.application.internal.commandservices;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.commands.UpdateAssessmentCommand;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByStudentQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.CourseStatus;
import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UpdateAssessmentCommandServiceImplTest {

    private final UUID teacherId = UUID.randomUUID();
    private final Instant opensAt = Instant.parse("2026-11-02T10:00:00Z");
    private final Instant closesAt = opensAt.plus(Duration.ofHours(2));
    private final Map<UUID, Assessment> store = new HashMap<>();
    private UpdateAssessmentCommandServiceImpl service;
    private Assessment draft;

    @BeforeEach
    void setUp() {
        Course course = Course.fromPersistence(UUID.randomUUID(), teacherId, teacherId,
                "POO-101", "POO", null, "ABC123", CourseStatus.ACTIVE, null, null);
        service = new UpdateAssessmentCommandServiceImpl(
                new InMemoryAssessmentRepository(),
                new CourseOwnershipGuard(new SingleCourseQueryService(course)));

        draft = Assessment.create(course.getId(), "Parcial", null);
        store.put(draft.getId(), draft);
    }

    private UpdateAssessmentCommand commandFor(UUID assessmentId, UUID requesterId, Instant opens, Instant closes) {
        return new UpdateAssessmentCommand(assessmentId, requesterId, "Parcial final", "Sin apuntes", opens, closes);
    }

    @Test
    void ownerEditsTheDraftAndChangesPersist() {
        var result = service.handle(commandFor(draft.getId(), teacherId, opensAt, closesAt));

        assertTrue(result.isSuccess());
        Assessment stored = store.get(draft.getId());
        assertEquals("Parcial final", stored.getTitle());
        assertEquals("Sin apuntes", stored.getInstructions());
        assertEquals(opensAt, stored.getOpensAt());
        assertEquals(closesAt, stored.getClosesAt());
    }

    @Test
    void publishedAssessmentRejectsEditionAsBusinessRuleViolation() {
        draft.addOpenQuestion("¿Qué es la encapsulación?", "modelo", null, List.of("oculta estado"));
        draft.publish(opensAt, closesAt);

        var result = service.handle(commandFor(draft.getId(), teacherId, opensAt, closesAt));

        assertEquals(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, result.getError().code());
        assertEquals("Parcial", store.get(draft.getId()).getTitle());
    }

    @Test
    void invalidScheduleIsAValidationError() {
        var result = service.handle(commandFor(draft.getId(), teacherId, closesAt, opensAt));

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, result.getError().code());
    }

    @Test
    void onlyTheCourseTeacherCanEdit() {
        var result = service.handle(commandFor(draft.getId(), UUID.randomUUID(), opensAt, closesAt));

        assertEquals(ApplicationErrorCode.FORBIDDEN, result.getError().code());
        assertEquals("Parcial", store.get(draft.getId()).getTitle());
    }

    @Test
    void unknownAssessmentIsNotFound() {
        var result = service.handle(commandFor(UUID.randomUUID(), teacherId, opensAt, closesAt));

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

        @Override
        public List<Course> handle(ListCoursesByStudentQuery query) {
            return List.of();
        }
    }
}
