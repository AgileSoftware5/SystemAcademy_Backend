package io.github.agilesoftware5.systemacademy.courses.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Enrollment;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByStudentQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.valueobjects.EnrollmentStatus;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

    private static final Logger log = LoggerFactory.getLogger(CourseQueryServiceImpl.class);

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseQueryServiceImpl(CourseRepository courseRepository,
                                  EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Optional<Course> handle(GetCourseByIdQuery query) {
        log.debug("consultando curso id={}", query.courseId());
        return courseRepository.findById(query.courseId());
    }

    @Override
    public List<Course> handle(ListCoursesByInstructorQuery query) {
        log.debug("listando cursos del instructor id={}", query.instructorId());
        return courseRepository.findByInstructorId(query.instructorId());
    }

    @Override
    public List<Course> handle(ListCoursesByStudentQuery query) {
        UUID studentId = query.studentId();

        log.debug("listando cursos del estudiante id={}", studentId);

        List<Enrollment> activeEnrollments =
                enrollmentRepository.findByStudentIdAndStatus(
                        studentId,
                        EnrollmentStatus.ACTIVE
                );

        if (activeEnrollments.isEmpty()) {
            return List.of();
        }

        List<UUID> courseIds = activeEnrollments.stream()
                .map(Enrollment::getCourseId)
                .toList();

        return courseRepository.findByIds(courseIds);
    }
}
