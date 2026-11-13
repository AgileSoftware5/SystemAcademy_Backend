package io.github.agilesoftware5.systemacademy.shared.domain;

import java.util.Optional;
import java.util.function.Function;

/**
 * Resultado explícito de un caso de uso: éxito con valor o fallo con {@link ApplicationError}.
 * Los command services devuelven esto en vez de lanzar excepciones de negocio.
 */
public final class Result<T> {

    private final T value;
    private final ApplicationError error;

    private Result(T value, ApplicationError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(ApplicationError error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        if (isFailure()) {
            throw new IllegalStateException("no se puede leer value de un Result en failure");
        }
        return value;
    }

    public ApplicationError getError() {
        if (isSuccess()) {
            throw new IllegalStateException("no se puede leer error de un Result en success");
        }
        return error;
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        return isSuccess() ? Result.success(mapper.apply(value)) : Result.failure(error);
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }
}
