package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionGradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.CriterionVerdict;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.GradingResult;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngine;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Motor de calificación que delega en el microservicio SystemAcademy_AI por HTTP.
 * Se activa con grading.engine=http. Todo fallo se traduce a GradingEngineException (mensaje apto para el docente) y
 * AnswerGrader lo registra como FAILED en la respuesta.
 */
@Component
@ConditionalOnProperty(name = "grading.engine", havingValue = "http")
public class HttpGradingEngine implements GradingEngine {

    private static final Logger log = LoggerFactory.getLogger(HttpGradingEngine.class);

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

        log.info("motor de IA por HTTP baseUrl={} readTimeout={}s", baseUrl, timeoutSeconds);
    }

    @Override
    public GradingResult grade(QuestionGradingInput input) {
        var request = new AiGradeRequest(
                input.questionId(),
                input.questionStem(),
                input.modelAnswer(),
                input.answerText(),
                input.criteria().stream()
                        .map(c -> new AiCriterion(c.rubricCriterionId(), c.description()))
                        .toList()
        );

        long startedAt = System.currentTimeMillis();
        log.debug("llamando servicio de IA questionId={} criteria={}", input.questionId(), input.criteria().size());

        AiGradeResponse response;
        try {
            response = client.post()
                    .uri("/api/v1/grade")
                    .body(request)
                    .retrieve()
                    .body(AiGradeResponse.class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (ResourceAccessException e) {
            throw isTimeout(e)
                    ? new GradingEngineException("el servicio de IA tardó demasiado en responder; vuelve a intentarlo", e)
                    : new GradingEngineException("no se pudo conectar con el servicio de IA; vuelve a intentarlo en unos minutos", e);
        } catch (RestClientException e) {
            throw new GradingEngineException("el servicio de IA devolvió una respuesta inválida", e);
        }

        log.debug("servicio de IA respondió questionId={} elapsedMs={}",
                input.questionId(), System.currentTimeMillis() - startedAt);

        if (response == null || response.criteria() == null) {
            throw new GradingEngineException("el servicio de IA devolvió una respuesta vacía");
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

    private static GradingEngineException translate(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String message = switch (status) {
            case 429 -> "el servicio de IA está saturado; vuelve a intentarlo en unos minutos";
            case 503 -> "el servicio de IA no está disponible; vuelve a intentarlo en unos minutos";
            case 504 -> "el servicio de IA tardó demasiado en responder; vuelve a intentarlo";
            default -> status >= 500
                    ? "el servicio de IA falló (HTTP " + status + "); vuelve a intentarlo"
                    : "el servicio de IA rechazó la solicitud (HTTP " + status + ")";
        };
        return new GradingEngineException(message, e);
    }

    private static boolean isTimeout(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.net.http.HttpTimeoutException
                    || cause instanceof java.net.SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    record AiCriterion(UUID rubricCriterionId, String description) {
    }

    record AiGradeRequest(
            UUID questionId,
            String questionStem,
            String modelAnswer,
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
