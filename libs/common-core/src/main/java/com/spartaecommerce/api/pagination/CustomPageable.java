package com.spartaecommerce.api.pagination;

public record CustomPageable(
    Integer page,
    Integer size,
    String sortBy,
    String direction,
    Long lastId
) {
    public CustomPageable {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }
        if (direction == null || direction.isBlank()) {
            direction = "DESC";
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Direction must be ASC or DESC");
        }
    }

    public static CustomPageable ofDefaults() {
        return new CustomPageable(0, 20, "createdAt", "DESC", null);
    }

    public static CustomPageable of(String sortBy, String direction) {
        return new CustomPageable(0, 20, sortBy, direction, null);
    }

    public static CustomPageable of(Integer page, Integer size, String sortBy, String direction) {
        return new CustomPageable(page - 1, size, sortBy, direction, null);
    }

    public static CustomPageable of(Integer page, Integer size, String sortBy, String direction, Long lastId) {
        return new CustomPageable(page - 1, size, sortBy, direction, lastId);
    }
}