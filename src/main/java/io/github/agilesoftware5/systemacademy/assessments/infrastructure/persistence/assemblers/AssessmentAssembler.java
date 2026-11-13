package io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.assemblers;

import io.github.agilesoftware5.systemacademy.assessments.domain.model.aggregates.Assessment;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.Question;
import io.github.agilesoftware5.systemacademy.assessments.domain.model.entities.RubricCriterion;
import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities.AssessmentEntity;
import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities.QuestionEntity;
import io.github.agilesoftware5.systemacademy.assessments.infrastructure.persistence.entities.RubricCriterionEntity;

import java.util.List;

/** Traduce el aggregate Assessment completo (con sus Question y RubricCriterion) hacia/desde el grafo JPA. */
public final class AssessmentAssembler {

    private AssessmentAssembler() {
    }

    public static Assessment toDomain(AssessmentEntity entity) {
        List<Question> questions = entity.getQuestions().stream()
                .map(AssessmentAssembler::questionToDomain)
                .toList();
        return Assessment.fromPersistence(
                entity.getId(),
                entity.getCourseId(),
                entity.getTitle(),
                entity.getInstructions(),
                entity.getOpensAt(),
                entity.getClosesAt(),
                entity.getStatus(),
                entity.getResultsReleasedAt(),
                questions
        );
    }

    private static Question questionToDomain(QuestionEntity entity) {
        List<RubricCriterion> criteria = entity.getCriteria().stream()
                .map(c -> RubricCriterion.fromPersistence(c.getId(), c.getPosition(), c.getDescription()))
                .toList();
        return Question.fromPersistence(entity.getId(), entity.getPosition(), entity.getStem(),
                entity.getModelAnswer(), entity.getExplanation(), criteria);
    }

    public static AssessmentEntity toEntity(Assessment assessment) {
        AssessmentEntity entity = new AssessmentEntity(
                assessment.getId(),
                assessment.getCourseId(),
                assessment.getTitle(),
                assessment.getInstructions(),
                assessment.getOpensAt(),
                assessment.getClosesAt(),
                assessment.getStatus(),
                assessment.getResultsReleasedAt()
        );
        for (Question question : assessment.getQuestions()) {
            entity.getQuestions().add(questionToEntity(question, entity));
        }
        return entity;
    }

    private static QuestionEntity questionToEntity(Question question, AssessmentEntity parent) {
        QuestionEntity entity = new QuestionEntity(question.getId(), parent, question.getPosition(),
                question.getStem(), question.getModelAnswer(), question.getExplanation());
        for (RubricCriterion criterion : question.getCriteria()) {
            entity.getCriteria().add(new RubricCriterionEntity(criterion.getId(), entity, criterion.getPosition(), criterion.getDescription()));
        }
        return entity;
    }
}
