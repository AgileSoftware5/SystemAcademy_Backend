package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionGradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.RubricCriterionInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "grading.engine", havingValue = "mock", matchIfMissing = true)
public class MockGradingEngine implements GradingEngine {

    @Override
    public GradingResult grade(QuestionGradingInput input) {

        List<CriterionGradingResult> criteria = input.criteria()
                .stream()
                .map(this::createMockCriterionResult)
                .toList();

        return new GradingResult(
                "La respuesta fue evaluada correctamente por el motor de evaluación simulado.",
                "mock-grading-engine",
                criteria
        );
    }

    private CriterionGradingResult createMockCriterionResult(
            RubricCriterionInput criterion
    ) {
        return new CriterionGradingResult(
                criterion.rubricCriterionId(),
                io.github.agilesoftware5.systemacademy.submissions
                        .domain.model.valueobjects.CriterionVerdict.MEETS,
                "La respuesta contiene información relacionada con el criterio.",
                "El criterio se considera cumplido en esta evaluación simulada."
        );
    }
}