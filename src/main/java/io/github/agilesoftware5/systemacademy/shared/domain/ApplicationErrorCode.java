package io.github.agilesoftware5.systemacademy.shared.domain;

/** Código de error de negocio, mapeado a HTTP en un único lugar (ver {@code GlobalExceptionHandler}). */
public enum ApplicationErrorCode {
    VALIDATION_ERROR,
    NOT_FOUND,
    BUSINESS_RULE_VIOLATION,
    CONFLICT,
    UNEXPECTED
}
