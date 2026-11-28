package io.github.agilesoftware5.systemacademy.analytics.infrastructure;

import io.github.agilesoftware5.systemacademy.analytics.application.dto.AnalyticsFilter;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.DayCount;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.DayUsers;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.GradingHealth;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.Kpis;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.TeacherRow;
import io.github.agilesoftware5.systemacademy.analytics.application.dto.OverviewReport.Verdicts;
import io.github.agilesoftware5.systemacademy.analytics.application.ports.AnalyticsReadModel;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Lado de lectura: SQL agregado sobre el esquema existente, sin cargar agregados del dominio. */
@Component
public class JdbcAnalyticsReadModel implements AnalyticsReadModel {

    private static final int MIN_SAMPLE = OverviewReport.MIN_SAMPLE;

    private final JdbcClient jdbc;

    public JdbcAnalyticsReadModel(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public OverviewReport overview(AnalyticsFilter f, LocalDate from, LocalDate to) {
        var scope = new Scope(f);

        long courses = count("SELECT count(*) FROM courses c WHERE " + scope.courses, scope.params);
        long enrollments = count("""
                SELECT count(*) FROM enrollments e JOIN courses c ON c.id = e.course_id
                WHERE e.status = 'ACTIVE' AND %s""".formatted(scope.courses), scope.params);
        long assessments = count("""
                SELECT count(*) FROM assessments a JOIN courses c ON c.id = a.course_id
                WHERE a.status <> 'DRAFT' AND %s""".formatted(scope.courses), scope.params);
        long openAssessments = count("""
                SELECT count(*) FROM assessments a JOIN courses c ON c.id = a.course_id
                WHERE a.status = 'PUBLISHED' AND (a.closes_at IS NULL OR a.closes_at > now()) AND %s""".formatted(scope.courses), scope.params);
        long enrolledStudents = count("""
                SELECT count(DISTINCT e.student_id) FROM enrollments e JOIN courses c ON c.id = e.course_id
                WHERE e.status = 'ACTIVE' AND %s""".formatted(scope.courses), scope.params);
        long submittingStudents = count("""
                SELECT count(DISTINCT s.student_id) FROM submissions s
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                JOIN enrollments e ON e.course_id = c.id AND e.student_id = s.student_id AND e.status = 'ACTIVE'
                WHERE s.status = 'SUBMITTED' AND s.submitted_at >= :from AND s.submitted_at < :to AND %s"""
                .formatted(scope.courses), scope.withRange(f));
        long submissions = count("""
                SELECT count(*) FROM submissions s
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.status = 'SUBMITTED' AND s.submitted_at >= :from AND s.submitted_at < :to AND %s"""
                .formatted(scope.courses), scope.withRange(f));

        var correction = jdbc.sql("""
                SELECT count(*) AS total, count(*) FILTER (WHERE cr.final_verdict <> cr.ai_verdict) AS changed,
                       count(*) FILTER (WHERE cr.final_verdict = 'MEETS') AS meets,
                       count(*) FILTER (WHERE cr.final_verdict = 'PARTIALLY_MEETS') AS partial,
                       count(*) FILTER (WHERE cr.final_verdict = 'DOES_NOT_MEET') AS none
                FROM criterion_results cr
                JOIN answers an ON an.id = cr.answer_id
                JOIN submissions s ON s.id = an.submission_id
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.submitted_at >= :from AND s.submitted_at < :to AND %s""".formatted(scope.courses))
                .params(scope.withRange(f))
                .query((rs, i) -> new long[]{rs.getLong("total"), rs.getLong("changed"), rs.getLong("meets"), rs.getLong("partial"), rs.getLong("none")})
                .single();

        var grading = jdbc.sql("""
                SELECT count(*) FILTER (WHERE an.grading_status = 'COMPLETED') AS completed,
                       count(*) FILTER (WHERE an.grading_status = 'PROCESSING') AS processing,
                       count(*) FILTER (WHERE an.grading_status = 'PENDING') AS pending,
                       count(*) FILTER (WHERE an.grading_status = 'FAILED') AS failed
                FROM answers an
                JOIN submissions s ON s.id = an.submission_id
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.submitted_at >= :from AND s.submitted_at < :to AND %s""".formatted(scope.courses))
                .params(scope.withRange(f))
                .query((rs, i) -> new GradingHealth(rs.getLong("completed"), rs.getLong("processing"), rs.getLong("pending"), rs.getLong("failed")))
                .single();

        var perDay = jdbc.sql("""
                SELECT (s.submitted_at AT TIME ZONE 'UTC')::date AS day, count(*) AS n
                FROM submissions s JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.status = 'SUBMITTED' AND s.submitted_at >= :from AND s.submitted_at < :to AND %s
                GROUP BY 1""".formatted(scope.courses))
                .params(scope.withRange(f))
                .query((rs, i) -> Map.entry(rs.getObject("day", LocalDate.class), rs.getLong("n")))
                .list().stream().collect(HashMap<LocalDate, Long>::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);

        Long students = null;
        Long teachers = null;
        List<DayUsers> newUsers = List.of();
        List<TeacherRow> teacherRows = List.of();
        if (f.includeUsers()) {
            var byRole = jdbc.sql("SELECT role, count(*) AS n FROM users WHERE status = 'ACTIVE' GROUP BY role")
                    .query((rs, i) -> Map.entry(rs.getString("role"), rs.getLong("n")))
                    .list().stream().collect(HashMap<String, Long>::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
            students = byRole.getOrDefault("STUDENT", 0L);
            teachers = byRole.getOrDefault("TEACHER", 0L);
            newUsers = newUsersByDay(f, from, to);
            teacherRows = teacherRows(f);
        } else {
            students = count("""
                    SELECT count(DISTINCT e.student_id) FROM enrollments e JOIN courses c ON c.id = e.course_id
                    WHERE e.status = 'ACTIVE' AND %s""".formatted(scope.courses), scope.params);
        }

        var days = new ArrayList<DayCount>();
        for (var d = from; !d.isAfter(to); d = d.plusDays(1)) {
            days.add(new DayCount(d, perDay.getOrDefault(d, 0L)));
        }

        var kpis = new Kpis(
                students, teachers, courses, enrollments, assessments, openAssessments, submissions,
                rate(submittingStudents, enrolledStudents, MIN_SAMPLE),
                rate(correction[1], correction[0], MIN_SAMPLE));
        return new OverviewReport(from, to, kpis, days, newUsers,
                new Verdicts(correction[2], correction[3], correction[4]), grading, teacherRows);
    }

    private List<DayUsers> newUsersByDay(AnalyticsFilter f, LocalDate from, LocalDate to) {
        var rows = jdbc.sql("""
                SELECT (created_at AT TIME ZONE 'UTC')::date AS day,
                       count(*) FILTER (WHERE role = 'STUDENT') AS students,
                       count(*) FILTER (WHERE role = 'TEACHER') AS teachers
                FROM users WHERE created_at >= :from AND created_at < :to GROUP BY 1""")
                .param("from", Timestamp.from(f.from())).param("to", Timestamp.from(f.toExclusive()))
                .query((rs, i) -> new DayUsers(rs.getObject("day", LocalDate.class), rs.getLong("students"), rs.getLong("teachers")))
                .list().stream().collect(HashMap<LocalDate, DayUsers>::new, (m, r) -> m.put(r.day(), r), HashMap::putAll);
        var out = new ArrayList<DayUsers>();
        for (var d = from; !d.isAfter(to); d = d.plusDays(1)) {
            out.add(rows.getOrDefault(d, new DayUsers(d, 0, 0)));
        }
        return out;
    }

    private List<TeacherRow> teacherRows(AnalyticsFilter f) {
        var range = Map.<String, Object>of("from", Timestamp.from(f.from()), "to", Timestamp.from(f.toExclusive()));

        var structure = keyed("""
                SELECT c.instructor_id AS id, count(DISTINCT c.id) AS courses,
                       count(DISTINCT e.student_id) AS students, count(e.id) AS enrollments
                FROM courses c LEFT JOIN enrollments e ON e.course_id = c.id AND e.status = 'ACTIVE'
                GROUP BY c.instructor_id""", Map.of(), "courses", "students", "enrollments");
        var activity = keyed("""
                SELECT c.instructor_id AS id, count(DISTINCT a.id) AS assessments,
                       count(DISTINCT s.id) FILTER (WHERE s.status = 'SUBMITTED' AND s.submitted_at >= :from AND s.submitted_at < :to) AS submissions,
                       count(DISTINCT s.student_id) FILTER (WHERE s.status = 'SUBMITTED' AND s.submitted_at >= :from AND s.submitted_at < :to) AS submitting
                FROM courses c LEFT JOIN assessments a ON a.course_id = c.id AND a.status <> 'DRAFT'
                LEFT JOIN submissions s ON s.assessment_id = a.id
                GROUP BY c.instructor_id""", range, "assessments", "submissions", "submitting");
        var quality = keyed("""
                SELECT c.instructor_id AS id, count(*) AS total,
                       count(*) FILTER (WHERE cr.final_verdict <> cr.ai_verdict) AS changed
                FROM criterion_results cr JOIN answers an ON an.id = cr.answer_id
                JOIN submissions s ON s.id = an.submission_id
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.submitted_at >= :from AND s.submitted_at < :to
                GROUP BY c.instructor_id""", range, "total", "changed");
        var failures = keyed("""
                SELECT c.instructor_id AS id, count(*) AS total,
                       count(*) FILTER (WHERE an.grading_status = 'FAILED') AS failed
                FROM answers an JOIN submissions s ON s.id = an.submission_id
                JOIN assessments a ON a.id = s.assessment_id JOIN courses c ON c.id = a.course_id
                WHERE s.submitted_at >= :from AND s.submitted_at < :to
                GROUP BY c.instructor_id""", range, "total", "failed");

        return jdbc.sql("SELECT id, full_name FROM users WHERE role = 'TEACHER' AND status = 'ACTIVE' ORDER BY full_name")
                .query((rs, i) -> {
                    var id = rs.getObject("id", UUID.class);
                    var st = structure.getOrDefault(id, new long[3]);
                    var ac = activity.getOrDefault(id, new long[3]);
                    var qu = quality.getOrDefault(id, new long[2]);
                    var fa = failures.getOrDefault(id, new long[2]);
                    return new TeacherRow(id, rs.getString("full_name"), st[0], st[1], ac[0], ac[1],
                            rate(ac[2], st[1], MIN_SAMPLE), rate(qu[1], qu[0], MIN_SAMPLE), rate(fa[1], fa[0], MIN_SAMPLE));
                }).list();
    }

    private Map<UUID, long[]> keyed(String sql, Map<String, Object> params, String... columns) {
        var out = new HashMap<UUID, long[]>();
        jdbc.sql(sql).params(params).query((rs, i) -> {
            var values = new long[columns.length];
            for (int c = 0; c < columns.length; c++) {
                values[c] = rs.getLong(columns[c]);
            }
            out.put(rs.getObject("id", UUID.class), values);
            return null;
        }).list();
        return out;
    }

    private long count(String sql, Map<String, Object> params) {
        return jdbc.sql(sql).params(params).query(Long.class).single();
    }

    private static Double rate(long part, long whole, int minSample) {
        return whole < minSample ? null : Math.round(part * 1000.0 / whole) / 1000.0;
    }

    private static final class Scope {
        final String courses;
        final Map<String, Object> params = new HashMap<>();

        Scope(AnalyticsFilter f) {
            var sql = new StringBuilder("1 = 1");
            if (f.teacherId() != null) {
                sql.append(" AND c.instructor_id = :teacher");
                params.put("teacher", f.teacherId());
            }
            if (f.courseId() != null) {
                sql.append(" AND c.id = :course");
                params.put("course", f.courseId());
            }
            this.courses = sql.toString();
        }

        Map<String, Object> withRange(AnalyticsFilter f) {
            var all = new HashMap<>(params);
            all.put("from", Timestamp.from(f.from()));
            all.put("to", Timestamp.from(f.toExclusive()));
            return all;
        }
    }
}
