package io.github.agilesoftware5.systemacademy.shared.domain;

/** Error de negocio tipado que devuelven los command services en un {@link Result} de failure. */
public record ApplicationError(ApplicationErrorCode code, String message) {

    public static ApplicationError validationError(String message) {
        return new ApplicationError(ApplicationErrorCode.VALIDATION_ERROR, message);
    }

    /** La sesión no es válida (token de un usuario que ya no existe o está inactivo). */
    public static ApplicationError unauthorized(String message) {
        return new ApplicationError(ApplicationErrorCode.UNAUTHORIZED, message);
    }

    public static ApplicationError notFound(String message) {
        return new ApplicationError(ApplicationErrorCode.NOT_FOUND, message);
    }

    /** Autenticado, pero sin derecho sobre ese recurso (no es su curso, su entrega, etc.). */
    public static ApplicationError forbidden(String message) {
        return new ApplicationError(ApplicationErrorCode.FORBIDDEN, message);
    }

    public static ApplicationError businessRuleViolation(String message) {
        return new ApplicationError(ApplicationErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public static ApplicationError conflict(String message) {
        return new ApplicationError(ApplicationErrorCode.CONFLICT, message);
    }

    public static ApplicationError unexpected(String message) {
        return new ApplicationError(ApplicationErrorCode.UNEXPECTED, message);
    }
}
