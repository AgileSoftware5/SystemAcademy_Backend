package io.github.agilesoftware5.systemacademy.submissions.domain.model.valueobjects;

public enum GradingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED;

    /** FAILED también se puede reintentar: un error del proveedor de IA no debe bloquear la respuesta. */
    public boolean canStartGrading() {
        return this == PENDING || this == FAILED;
    }
}
