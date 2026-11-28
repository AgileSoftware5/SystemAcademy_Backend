package io.github.agilesoftware5.systemacademy.analytics.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OverviewReport(
        LocalDate from,
        LocalDate to,
        Kpis kpis,
        List<DayCount> submissionsByDay,
        List<DayUsers> newUsersByDay,
        Verdicts verdicts,
        GradingHealth grading,
        List<TeacherRow> teachers
) {

    public record Kpis(
            Long students,
            Long teachers,
            long courses,
            long enrollments,
            long assessments,
            long openAssessments,
            long submissions,
            Double participationRate,
            Double teacherCorrectionRate
    ) {
    }

    public record DayCount(LocalDate day, long count) {
    }

    public record DayUsers(LocalDate day, long students, long teachers) {
    }

    public record Verdicts(long meets, long partiallyMeets, long doesNotMeet) {
    }

    public record GradingHealth(long completed, long processing, long pending, long failed) {
    }

    /** Las tasas llegan en null cuando la muestra es menor que {@link #MIN_SAMPLE}: así no se identifica a nadie ni se concluye con muy pocos datos. */
    public record TeacherRow(
            UUID teacherId,
            String name,
            long courses,
            long students,
            long assessments,
            long submissions,
            Double participationRate,
            Double correctionRate,
            Double failedRate
    ) {
    }

    public static final int MIN_SAMPLE = 5;
}
