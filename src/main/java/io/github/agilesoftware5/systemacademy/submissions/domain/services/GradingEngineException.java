package io.github.agilesoftware5.systemacademy.submissions.domain.services;

/**
 * Fallo del motor de calificación. El mensaje es apto para mostrarse al docente:
 * el detalle técnico (host, stack) va en la causa y solo se registra en logs.
 */
public class GradingEngineException extends RuntimeException {

    public GradingEngineException(String message) {
        super(message);
    }

    public GradingEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
