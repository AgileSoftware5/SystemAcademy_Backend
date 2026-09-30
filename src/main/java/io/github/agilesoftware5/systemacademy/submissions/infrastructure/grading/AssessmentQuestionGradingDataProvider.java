package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.Question;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.RubricCriterion;
import io.github.agilesoftware5.systemacademy.assessments.domain.repositories.AssessmentRepository;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.RubricCriterionInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.QuestionGradingDataProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AssessmentQuestionGradingDataProvider
        implements QuestionGradingDataProvider {

    private final AssessmentRepository assessmentRepository;

    public AssessmentQuestionGradingDataProvider(
            AssessmentRepository assessmentRepository
    ) {
        this.assessmentRepository = assessmentRepository;
    }

    @Override
    public QuestionGradingInput getQuestionGradingInput(
            UUID assessmentId,
            UUID questionId,
            String answerText
    ) {

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "evaluación no encontrada"
                        )
                );

        Question question = assessment.getQuestions()
                .stream()
                .filter(existing ->
                        existing.getId().equals(questionId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "la pregunta no pertenece a la evaluación"
                        )
                );

        List<RubricCriterionInput> criteria = question.getCriteria()
                .stream()
                .map(this::toRubricCriterionInput)
                .toList();

        return new QuestionGradingInput(
                question.getId(),
                question.getStem(),
                answerText,
                criteria
        );
    }

    private RubricCriterionInput toRubricCriterionInput(
            RubricCriterion criterion
    ) {
        return new RubricCriterionInput(
                criterion.getId(),
                criterion.getDescription()
        );
    }
}