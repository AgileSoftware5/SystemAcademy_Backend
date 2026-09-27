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

    /**
     * @param includeModelAnswer false para alumnos: la respuesta modelo nunca debe viajar a quien va a rendir
     *                           la evaluación, o el resultado de la corrección pierde sentido.
     */
    public static AssessmentResource toResource(Assessment assessment, boolean includeModelAnswer) {
        var questions = assessment.getQuestions().stream()
                .map(q -> toResource(q, includeModelAnswer))
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

    private static QuestionResource toResource(Question question, boolean includeModelAnswer) {
        var criteria = question.getCriteria().stream()
                .map(AssessmentResourceFromDomainAssembler::toResource)
                .toList();
        String modelAnswer = includeModelAnswer ? question.getModelAnswer() : null;
        return new QuestionResource(question.getId(), question.getPosition(), question.getStem(),
                modelAnswer, question.getExplanation(), criteria);
    }

    private static RubricCriterionResource toResource(RubricCriterion criterion) {
        return new RubricCriterionResource(criterion.getId(), criterion.getPosition(), criterion.getDescription());
    }
}
