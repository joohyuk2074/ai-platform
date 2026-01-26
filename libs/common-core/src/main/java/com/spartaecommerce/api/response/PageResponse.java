package com.spartaecommerce.api.response;

import java.util.List;

public record PageResponse<T>(
    List<T> contents,
    Integer page,
    Integer size,
    Long totalElements,
    Integer totalPages
) {
    public static <T> PageResponse<T> of(List<T> contents) {
        return new PageResponse<>(contents, null, null, null, null);
    }

    public static <T> PageResponse<T> of(
        List<T> contents,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
    ) {
        return new PageResponse<>(contents, page, size, totalElements, totalPages);
    }
}
