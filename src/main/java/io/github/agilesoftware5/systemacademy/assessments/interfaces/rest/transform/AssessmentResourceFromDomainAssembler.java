package io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.transform;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.Question;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.RubricCriterion;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.AssessmentResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.QuestionResource;
import io.github.agilesoftware5.systemacademy.assessments.interfaces.rest.resources.RubricCriterionResource;

public final class AssessmentResourceFromDomainAssembler {

    private AssessmentResourceFromDomainAssembler() {
    }

    public static AssessmentResource toResource(Assessment assessment) {
        var questions = assessment.getQuestions().stream()
                .map(AssessmentResourceFromDomainAssembler::toResource)
                .toList();
        return new AssessmentResource(
                assessment.getId(),
                assessment.getCourseId(),
                assessment.getTitle(),
                assessment.getInstructions(),
                assessment.getOpensAt(),
                assessment.getClosesAt(),
                assessment.getStatus().name(),
                questions
        );
    }

    private static QuestionResource toResource(Question question) {
        var criteria = question.getCriteria().stream()
                .map(AssessmentResourceFromDomainAssembler::toResource)
                .toList();
        return new QuestionResource(question.getId(), question.getPosition(), question.getStem(),
                question.getModelAnswer(), question.getExplanation(), criteria);
    }

    private static RubricCriterionResource toResource(RubricCriterion criterion) {
        return new RubricCriterionResource(criterion.getId(), criterion.getPosition(), criterion.getDescription());
    }
}
