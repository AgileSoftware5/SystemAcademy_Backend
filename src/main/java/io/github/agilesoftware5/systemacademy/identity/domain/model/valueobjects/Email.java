package io.github.agilesoftware5.systemacademy.identity.domain.model.valueobjects;

import java.util.regex.Pattern;

/** Value object: valida formato en construcción, nunca existe un Email inválido en el dominio. */
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("email inválido");
        }
    }
}
