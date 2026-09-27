package io.github.agilesoftware5.systemacademy.courses.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.CourseQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.aggregates.Course;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.GetCourseByIdQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.ListCoursesByInstructorQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

    private static final Logger log = LoggerFactory.getLogger(CourseQueryServiceImpl.class);

    private final CourseRepository courseRepository;

    public CourseQueryServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
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
}
