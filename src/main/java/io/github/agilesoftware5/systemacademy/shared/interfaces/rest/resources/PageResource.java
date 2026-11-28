package io.github.agilesoftware5.systemacademy.shared.interfaces.rest.resources;

import io.github.agilesoftware5.systemacademy.shared.domain.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResource<T>(List<T> items, long total, int page, int size) {

    public static <S, T> PageResource<T> from(PageResult<S> page, Function<S, T> mapper) {
        return new PageResource<>(page.items().stream().map(mapper).toList(), page.total(), page.page(), page.size());
    }
}
