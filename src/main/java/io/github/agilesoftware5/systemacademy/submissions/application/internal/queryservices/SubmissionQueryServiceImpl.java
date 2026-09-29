package io.github.agilesoftware5.systemacademy.submissions.application.internal.queryservices;

import io.github.agilesoftware5.systemacademy.submissions.application.queryservices.SubmissionQueryService;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.aggregates.Submission;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.GetSubmissionByIdQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByAssessmentQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.queries.ListSubmissionsByStudentQuery;
import io.github.agilesoftware5.systemacademy.submissions.domain.repositories.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubmissionQueryServiceImpl
        implements SubmissionQueryService {

    private static final Logger log =
            LoggerFactory.getLogger(SubmissionQueryServiceImpl.class);

    private final SubmissionRepository submissionRepository;

    public SubmissionQueryServiceImpl(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Optional<Submission> handle(
            GetSubmissionByIdQuery query
    ) {
        log.debug(
                "consultando entrega id={}",
                query.submissionId()
        );

        return submissionRepository.findById(
                query.submissionId()
        );
    }

    @Override
    public List<Submission> handle(
            ListSubmissionsByStudentQuery query
    ) {
        log.debug(
                "listando entregas del estudiante id={}",
                query.studentId()
        );

        return submissionRepository.findByStudentId(
                query.studentId()
        );
    }

    @Override
    public List<Submission> handle(
            ListSubmissionsByAssessmentQuery query
    ) {
        log.debug(
                "listando entregas de la evaluación id={}",
                query.assessmentId()
        );

        return submissionRepository.findByAssessmentId(
                query.assessmentId()
        );
    }
}
