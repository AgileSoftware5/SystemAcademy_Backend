package io.github.agilesoftware5.systemacademy.shared.domain;

/** Código de error de negocio, mapeado a HTTP en un único lugar (ver {@code GlobalExceptionHandler}). */
public enum ApplicationErrorCode {
    VALIDATION_ERROR,
    UNAUTHORIZED,
    NOT_FOUND,
    FORBIDDEN,
    BUSINESS_RULE_VIOLATION,
    CONFLICT,
    UNEXPECTED
}
