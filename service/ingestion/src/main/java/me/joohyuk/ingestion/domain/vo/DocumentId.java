package me.joohyuk.ingestion.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * 문서 식별자 값 객체
 *
 * 불변 객체로 문서의 고유 식별자를 표현합니다.
 * UUID를 사용하여 글로벌하게 유일한 ID를 보장합니다.
 */
public final class DocumentId {

    private final String value;

    private DocumentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }
        this.value = value;
    }

    /**
     * 새로운 Document ID 생성
     */
    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID().toString());
    }

    /**
     * 기존 ID 값으로부터 Document ID 재구성
     */
    public static DocumentId of(String value) {
        return new DocumentId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentId that = (DocumentId) o;
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
