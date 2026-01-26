package me.joohyuk.ingestion.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * 문서 컬렉션 식별자 값 객체
 */
public final class CollectionId {

    private final String value;

    private CollectionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Collection ID cannot be null or empty");
        }
        this.value = value;
    }

    public static CollectionId generate() {
        return new CollectionId(UUID.randomUUID().toString());
    }

    public static CollectionId of(String value) {
        return new CollectionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionId that = (CollectionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
