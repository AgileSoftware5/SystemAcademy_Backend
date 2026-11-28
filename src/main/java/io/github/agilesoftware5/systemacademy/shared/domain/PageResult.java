package io.github.agilesoftware5.systemacademy.shared.domain;

import java.util.List;

public record PageResult<T>(List<T> items, long total, int page, int size) {
}
