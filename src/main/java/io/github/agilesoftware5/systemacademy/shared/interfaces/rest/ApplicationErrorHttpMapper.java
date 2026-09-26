package io.github.agilesoftware5.systemacademy.shared.interfaces.rest;

import io.github.agilesoftware5.systemacademy.shared.domain.ApplicationError;
import io.github.agilesoftware5.systemacademy.shared.interfaces.rest.resources.ErrorResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Único lugar donde un {@link io.github.agilesoftware5.systemacademy.shared.domain.ApplicationErrorCode} se traduce a HTTP. */
public final class ApplicationErrorHttpMapper {

    private ApplicationErrorHttpMapper() {
    }

    public static ResponseEntity<ErrorResource> toResponse(ApplicationError error) {
        HttpStatus status = switch (error.code()) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BUSINESS_RULE_VIOLATION -> HttpStatus.UNPROCESSABLE_CONTENT;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(new ErrorResource(error.code().name(), error.message()));
    }
}
