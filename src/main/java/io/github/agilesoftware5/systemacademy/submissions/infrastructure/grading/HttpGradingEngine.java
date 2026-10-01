package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionGradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Motor de calificación que delega en el microservicio SystemAcademy_AI por HTTP.
 * Se activa con grading.engine=http. Cualquier fallo se propaga como excepción y
 * GradeAnswerCommandServiceImpl lo registra como FAILED en la respuesta.
 */
@Component
@ConditionalOnProperty(name = "grading.engine", havingValue = "http")
public class HttpGradingEngine implements GradingEngine {

    private final RestClient client;

    public HttpGradingEngine(
            @Value("${grading.ai.base-url}") String baseUrl,
            @Value("${grading.ai.api-key:}") String apiKey,
            @Value("${grading.ai.timeout-seconds:90}") int timeoutSeconds
    ) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build()
        );
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        var builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory);

        if (!apiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", apiKey);
        }

        this.client = builder.build();
    }

    @Override
    public GradingResult grade(QuestionGradingInput input) {
        var request = new AiGradeRequest(
                input.questionId(),
                input.questionStem(),
                input.answerText(),
                input.criteria().stream()
                        .map(c -> new AiCriterion(c.rubricCriterionId(), c.description()))
                        .toList()
        );

        var response = client.post()
                .uri("/api/v1/grade")
                .body(request)
                .retrieve()
                .body(AiGradeResponse.class);

        if (response == null || response.criteria() == null) {
            throw new IllegalStateException("el servicio de IA devolvió una respuesta vacía");
        }

        return new GradingResult(
                response.generalFeedback(),
                response.model(),
                response.criteria().stream()
                        .map(c -> new CriterionGradingResult(
                                c.rubricCriterionId(),
                                c.verdict(),
                                c.evidence(),
                                c.feedback()
                        ))
                        .toList()
        );
    }

    record AiCriterion(UUID rubricCriterionId, String description) {
    }

    record AiGradeRequest(
            UUID questionId,
            String questionStem,
            String answerText,
            List<AiCriterion> criteria
    ) {
    }

    record AiCriterionResult(
            UUID rubricCriterionId,
            CriterionVerdict verdict,
            String evidence,
            String feedback
    ) {
    }

    record AiGradeResponse(
            String generalFeedback,
            String model,
            List<AiCriterionResult> criteria
    ) {
    }
}
