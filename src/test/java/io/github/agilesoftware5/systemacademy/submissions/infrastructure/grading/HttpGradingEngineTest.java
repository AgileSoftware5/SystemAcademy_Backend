package io.github.agilesoftware5.systemacademy.submissions.infrastructure.grading;

import com.sun.net.httpserver.HttpServer;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.QuestionGradingInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects.RubricCriterionInput;
import io.github.agilesoftware5.systemacademy.submissions.domain.services.GradingEngineException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpGradingEngineTest {

    private final UUID criterion = UUID.randomUUID();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private HttpGradingEngine engineAgainst(int status, String body, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/grade", exchange -> {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException ignored) {
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
        return new HttpGradingEngine("http://127.0.0.1:" + server.getAddress().getPort(), "", 1);
    }

    private QuestionGradingInput input() {
        return new QuestionGradingInput(UUID.randomUUID(), "stem", "model", "respuesta",
                List.of(new RubricCriterionInput(criterion, "c")));
    }

    private String messageOfFailure(HttpGradingEngine engine) {
        return assertThrows(GradingEngineException.class, () -> engine.grade(input())).getMessage();
    }

    @Test
    void validResponseIsMapped() throws IOException {
        var body = """
                {"generalFeedback":"ok","model":"m","criteria":[
                  {"rubricCriterionId":"%s","verdict":"MEETS","evidence":"e","feedback":"f"}]}
                """.formatted(criterion);

        var result = engineAgainst(200, body, 0).grade(input());

        assertEquals("m", result.model());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void connectionRefusedIsReportedAsUnavailable() throws IOException {
        var engine = engineAgainst(200, "{}", 0);
        server.stop(0);

        assertTrue(messageOfFailure(engine).contains("no se pudo conectar"));
    }

    @Test
    void readTimeoutIsReportedAsTimeout() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(200, "{}", 3000)).contains("tardó demasiado"));
    }

    @Test
    void status429IsReportedAsSaturated() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(429, "{}", 0)).contains("saturado"));
    }

    @Test
    void status503IsReportedAsUnavailable() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(503, "{}", 0)).contains("no está disponible"));
    }

    @Test
    void status504IsReportedAsTimeout() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(504, "{}", 0)).contains("tardó demasiado"));
    }

    @Test
    void otherServerErrorsIncludeTheStatus() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(500, "boom", 0)).contains("HTTP 500"));
    }

    @Test
    void clientErrorsAreReportedAsRejected() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(422, "{}", 0)).contains("rechazó la solicitud (HTTP 422)"));
    }

    @Test
    void malformedBodyIsReportedAsInvalidResponse() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(200, "no es json", 0)).contains("respuesta inválida"));
    }

    @Test
    void emptyCriteriaPayloadIsReportedAsEmpty() throws IOException {
        assertTrue(messageOfFailure(engineAgainst(200, "{}", 0)).contains("respuesta vacía"));
    }

    @Test
    void messagesNeverLeakTheInternalHost() throws IOException {
        var engine = engineAgainst(200, "{}", 0);
        server.stop(0);

        assertFalse(messageOfFailure(engine).contains("127.0.0.1"));
    }
}
