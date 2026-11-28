package io.github.agilesoftware5.systemacademy.analytics.infrastructure;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.AnalyticsFilter;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditEntry;
import io.github.agilesoftware5.systemacademy.shared.infrastructure.audit.JdbcAuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Consultas de métricas y auditoría contra Postgres real, con el esquema creado por las migraciones de Flyway. */
@Testcontainers(disabledWithoutDocker = true)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.flyway.enabled=true")
@Import({JdbcAnalyticsReadModel.class, JdbcAuditLog.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JdbcAnalyticsReadModelIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16.4-alpine");

    @Autowired
    JdbcClient jdbc;

    @Autowired
    JdbcAnalyticsReadModel readModel;

    @Autowired
    JdbcAuditLog auditLog;

    private final LocalDate today = LocalDate.now(ZoneOffset.UTC);
    private UUID teacherA;
    private UUID teacherB;
    private UUID courseA;

    @BeforeEach
    void seed() {
        for (var table : new String[]{"criterion_results", "answers", "submissions", "rubric_criteria", "questions", "assessments", "enrollments", "courses", "users"}) {
            jdbc.sql("DELETE FROM " + table).update();
        }
        teacherA = user("TEACHER", "ta@x.edu");
        teacherB = user("TEACHER", "tb@x.edu");
        courseA = course(teacherA, "A-1");
        var courseB = course(teacherB, "B-1");

        var assessmentA = assessment(courseA);
        for (int i = 0; i < 6; i++) {
            var student = user("STUDENT", "s" + i + "@x.edu");
            enroll(courseA, student);
            var submission = submission(assessmentA, student);
            var answer = answer(submission, i == 0 ? "FAILED" : "COMPLETED");
            criterion(answer, "MEETS", i < 2 ? "PARTIALLY_MEETS" : "MEETS");
        }
        var lone = user("STUDENT", "lone@x.edu");
        enroll(courseB, lone);
        submission(assessment(courseB), lone);
    }

    private AnalyticsFilter filter(UUID teacher, UUID course, boolean users) {
        return new AnalyticsFilter(
                today.minusDays(6).atStartOfDay().toInstant(ZoneOffset.UTC),
                today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                teacher, course, users);
    }

    @Test
    void institutionOverviewCountsEverythingAndFillsEveryDay() {
        var report = readModel.overview(filter(null, null, true), today.minusDays(6), today);

        assertEquals(2, report.kpis().courses());
        assertEquals(7, report.kpis().enrollments());
        assertEquals(7, report.kpis().submissions());
        assertEquals(7, report.submissionsByDay().size());
        assertEquals(7, report.submissionsByDay().get(6).count());
        assertEquals(7L, report.kpis().students());
        assertEquals(2L, report.kpis().teachers());
        assertEquals(1, report.grading().failed());
        assertEquals(4, report.verdicts().meets());
        assertEquals(2, report.verdicts().partiallyMeets());
        assertEquals(2, report.teachers().size());
    }

    @Test
    void teacherScopeOnlySeesTheirOwnCoursesAndNoInstitutionUsers() {
        var report = readModel.overview(filter(teacherA, null, false), today.minusDays(6), today);

        assertEquals(1, report.kpis().courses());
        assertEquals(6, report.kpis().enrollments());
        assertEquals(6, report.kpis().submissions());
        assertEquals(6L, report.kpis().students());
        assertNull(report.kpis().teachers());
        assertTrue(report.teachers().isEmpty());
    }

    @Test
    void ratesAreHiddenWhenTheSampleIsTooSmall() {
        var report = readModel.overview(filter(null, null, true), today.minusDays(6), today);

        var a = report.teachers().stream().filter(t -> t.teacherId().equals(teacherA)).findFirst().orElseThrow();
        var b = report.teachers().stream().filter(t -> t.teacherId().equals(teacherB)).findFirst().orElseThrow();
        assertEquals(1.0, a.participationRate());
        assertEquals(0.333, a.correctionRate());
        assertNull(b.participationRate());
        assertNull(b.correctionRate());
    }

    @Test
    void filteringByCourseNarrowsTheNumbers() {
        var report = readModel.overview(filter(null, courseA, false), today.minusDays(6), today);

        assertEquals(1, report.kpis().courses());
        assertEquals(6, report.kpis().submissions());
    }

    @Test
    void submissionsOutsideTheRangeAreIgnored() {
        var report = readModel.overview(
                new AnalyticsFilter(Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(50, ChronoUnit.DAYS), null, null, true),
                today.minusDays(60), today.minusDays(50));

        assertEquals(0, report.kpis().submissions());
        assertEquals(0, report.verdicts().meets());
    }

    @Test
    void auditEntriesAreStored() {
        var actor = UUID.randomUUID();
        auditLog.record(new AuditEntry(actor, "USER_SUSPENDED", "USER", UUID.randomUUID(), "STUDENT"));

        var action = jdbc.sql("SELECT action FROM audit_log WHERE actor_id = :a").param("a", actor).query(String.class).single();
        assertEquals("USER_SUSPENDED", action);
        assertNotNull(jdbc.sql("SELECT created_at FROM audit_log WHERE actor_id = :a").param("a", actor).query(Object.class).single());
    }

    private UUID user(String role, String email) {
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, created_at, updated_at, email, full_name, password_hash, role, status) VALUES (:id, now(), now(), :e, :n, 'x', :r, 'ACTIVE')")
                .param("id", id).param("e", email).param("n", email).param("r", role).update();
        return id;
    }

    private UUID course(UUID teacher, String code) {
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO courses (id, created_at, updated_at, code, created_by, enrollment_code, instructor_id, name, status) VALUES (:id, now(), now(), :c, :t, :c, :t, :c, 'ACTIVE')")
                .param("id", id).param("c", code).param("t", teacher).update();
        return id;
    }

    private void enroll(UUID course, UUID student) {
        jdbc.sql("INSERT INTO enrollments (id, course_id, enrolled_at, status, student_id) VALUES (:id, :c, now(), 'ACTIVE', :s)")
                .param("id", UUID.randomUUID()).param("c", course).param("s", student).update();
    }

    private UUID assessment(UUID course) {
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO assessments (id, created_at, updated_at, course_id, status, title) VALUES (:id, now(), now(), :c, 'PUBLISHED', 't')")
                .param("id", id).param("c", course).update();
        return id;
    }

    private UUID submission(UUID assessment, UUID student) {
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO submissions (id, assessment_id, started_at, status, student_id, submitted_at) VALUES (:id, :a, now(), 'SUBMITTED', :s, now())")
                .param("id", id).param("a", assessment).param("s", student).update();
        return id;
    }

    private UUID answer(UUID submission, String status) {
        var assessment = jdbc.sql("SELECT assessment_id FROM submissions WHERE id = :s").param("s", submission).query(UUID.class).single();
        var question = UUID.randomUUID();
        jdbc.sql("INSERT INTO questions (id, created_at, updated_at, \"position\", question_type, stem, assessment_id) VALUES (:id, now(), now(), 1, 'OPEN', 'q', :a)")
                .param("id", question).param("a", assessment).update();
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO answers (id, grading_status, question_id, updated_at, submission_id) VALUES (:id, :g, :q, now(), :s)")
                .param("id", id).param("g", status).param("q", question).param("s", submission).update();
        return id;
    }

    private void criterion(UUID answer, String ai, String fin) {
        var question = jdbc.sql("SELECT question_id FROM answers WHERE id = :a").param("a", answer).query(UUID.class).single();
        var rubric = UUID.randomUUID();
        jdbc.sql("INSERT INTO rubric_criteria (id, description, \"position\", question_id) VALUES (:id, 'd', 1, :q)")
                .param("id", rubric).param("q", question).update();
        jdbc.sql("INSERT INTO criterion_results (id, ai_verdict, final_verdict, rubric_criterion_id, answer_id) VALUES (:id, :ai, :f, :r, :a)")
                .param("id", UUID.randomUUID()).param("ai", ai).param("f", fin).param("r", rubric).param("a", answer).update();
    }
}
