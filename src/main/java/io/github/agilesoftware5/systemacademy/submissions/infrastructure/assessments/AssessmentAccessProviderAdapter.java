package io.github.agilesoftware5.systemacademy.submissions.infrastructure.assessments;

import io.github.agilesoftware5.systemacademy.assessments.application.queryservices.AssessmentQueryService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.GetAssessmentByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.application.queryservices.EnrollmentQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.IsStudentEnrolledQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.AssessmentAccessProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssessmentAccessProviderAdapter implements AssessmentAccessProvider {

    private final AssessmentQueryService assessmentQueryService;
    private final CourseQueryService courseQueryService;
    private final EnrollmentQueryService enrollmentQueryService;

    public AssessmentAccessProviderAdapter(AssessmentQueryService assessmentQueryService,
                                           CourseQueryService courseQueryService,
                                           EnrollmentQueryService enrollmentQueryService) {
        this.assessmentQueryService = assessmentQueryService;
        this.courseQueryService = courseQueryService;
        this.enrollmentQueryService = enrollmentQueryService;
    }

    @Override
    public boolean isTaughtBy(UUID assessmentId, UUID teacherId) {
        return find(assessmentId)
                .flatMap(assessment -> courseQueryService.handle(new GetCourseByIdQuery(assessment.getCourseId())))
                .map(course -> course.getInstructorId().equals(teacherId))
                .orElse(false);
    }

    @Override
    public boolean areResultsReleased(UUID assessmentId) {
        return find(assessmentId).map(Assessment::areResultsReleased).orElse(false);
    }

    @Override
    public boolean isOpenForSubmissions(UUID assessmentId, Instant now) {
        return find(assessmentId).map(assessment -> assessment.isOpenForSubmissions(now)).orElse(false);
    }

    @Override
    public boolean isStudentEnrolled(UUID assessmentId, UUID studentId) {
        return find(assessmentId)
                .map(assessment -> enrollmentQueryService.handle(
                        new IsStudentEnrolledQuery(assessment.getCourseId(), studentId)))
                .orElse(false);
    }

    @Override
    public boolean hasQuestion(UUID assessmentId, UUID questionId) {
        return find(assessmentId)
                .map(assessment -> assessment.getQuestions().stream()
                        .anyMatch(question -> question.getId().equals(questionId)))
                .orElse(false);
    }

    private Optional<Assessment> find(UUID assessmentId) {
        return assessmentQueryService.handle(new GetAssessmentByIdQuery(assessmentId));
    }
}
