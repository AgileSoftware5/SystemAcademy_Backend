package io.github.agilesoftware5.systemacademy.assessments.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.assessments.application.queryservices.AssessmentQueryService;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.GetAssessmentByIdQuery;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.queries.ListAssessmentsByCourseQuery;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentQueryServiceImpl implements AssessmentQueryService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentQueryServiceImpl.class);

    private final AssessmentRepository assessmentRepository;

    public AssessmentQueryServiceImpl(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    @Override
    public Optional<Assessment> handle(GetAssessmentByIdQuery query) {
        log.debug("consultando evaluación id={}", query.assessmentId());
        return assessmentRepository.findById(query.assessmentId());
    }

    @Override
    public List<Assessment> handle(ListAssessmentsByCourseQuery query) {
        log.debug("listando evaluaciones del curso id={}", query.courseId());
        return assessmentRepository.findByCourseId(query.courseId());
    }
}
