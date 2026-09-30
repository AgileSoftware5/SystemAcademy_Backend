package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.RubricCriterionInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.QuestionGradingDataProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MockQuestionGradingDataProvider
        implements QuestionGradingDataProvider {

    @Override
    public QuestionGradingInput getQuestionGradingInput(
            UUID questionId,
            String answerText
    ) {

        return new QuestionGradingInput(
                questionId,
                "¿Qué es la encapsulación en programación orientada a objetos y cuál es su propósito?",
                answerText,
                List.of(
                        new RubricCriterionInput(
                                UUID.fromString(
                                        "d4d07a77-cbe9-440c-b7f5-a7a3786d19a5"
                                ),
                                "Explica que la encapsulación oculta o protege el estado interno del objeto."
                        ),
                        new RubricCriterionInput(
                                UUID.fromString(
                                        "3cbdfe72-97a3-4b07-adc9-5c5d3a38a4c6"
                                ),
                                "Explica que el acceso a los datos se controla mediante métodos o una interfaz pública."
                        ),
                        new RubricCriterionInput(
                                UUID.fromString(
                                        "0537548c-8d8d-4643-a759-c00b08fbeb1"
                                ),
                                "Explica que la encapsulación ayuda a proteger los datos y reducir el acoplamiento."
                        )
                )
        );
    }
}