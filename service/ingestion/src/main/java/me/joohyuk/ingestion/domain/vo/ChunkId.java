package me.joohyuk.ingestion.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * 청크 식별자 값 객체
 *
 * 청킹된 텍스트 조각의 고유 식별자를 표현합니다.
 */
public final class ChunkId {

    private final String value;

    private ChunkId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Chunk ID cannot be null or empty");
        }
        this.value = value;
    }

    public static ChunkId generate() {
        return new ChunkId(UUID.randomUUID().toString());
    }

    public static ChunkId of(String value) {
        return new ChunkId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkId chunkId = (ChunkId) o;
        return Objects.equals(value, chunkId.value);
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
