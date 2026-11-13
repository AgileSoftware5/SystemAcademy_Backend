package io.github.agilesoftware5.systemacademy.submissions.interfaces.rest.transform;

/** Cuánto de la calificación ve quien consulta una entrega. */
public enum GradingVisibility {
    /** Docente: todo, incluido el veredicto original de la IA. */
    FULL,
    /** Alumno con resultados publicados: solo el veredicto final, sin datos internos. */
    RELEASED,
    /** Alumno antes de la publicación: solo sus respuestas. */
    HIDDEN;

    public static GradingVisibility forStudent(boolean resultsReleased) {
        return resultsReleased ? RELEASED : HIDDEN;
    }
}
