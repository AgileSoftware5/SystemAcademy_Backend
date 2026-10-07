package io.github.agilesoftware5.systemacademy.assessments.application.internal.outboundservices;

import java.util.UUID;

/**
 * Puerto de salida hacia el contexto submissions. Lo implementa submissions (no al revés)
 * para que la dependencia entre contextos vaya en una sola dirección.
 */
@FunctionalInterface
public interface GradingProgressService {

    /** Respuestas de entregas ya enviadas que todavía no terminaron de calificarse. */
    long countUngradedAnswers(UUID assessmentId);
}
