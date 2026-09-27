package io.github.agilesoftware5.systemacademy.courses.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.courses.application.queryservices.EnrollmentQueryService;
import io.github.agilesoftware5.systemacademy.courses.domain.model.queries.IsStudentEnrolledQuery;
import io.github.agilesoftware5.systemacademy.courses.domain.repositories.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentQueryServiceImpl.class);

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentQueryServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public boolean handle(IsStudentEnrolledQuery query) {
        log.debug("verificando matrícula courseId={} studentId={}", query.courseId(), query.studentId());
        return enrollmentRepository.existsByCourseIdAndStudentId(query.courseId(), query.studentId());
    }
}
