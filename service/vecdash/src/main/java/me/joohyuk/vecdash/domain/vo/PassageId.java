package me.joohyuk.vecdash.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Passage 식별자 값 객체
 *
 * 임베딩 단위의 고유 식별자를 표현합니다.
 * Vector DB에 저장될 때 사용되는 Primary Key 역할을 합니다.
 */
public final class PassageId {

    private final String value;

    private PassageId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Passage ID cannot be null or empty");
        }
        this.value = value;
    }

    public static PassageId generate() {
        return new PassageId(UUID.randomUUID().toString());
    }

    public static PassageId of(String value) {
        return new PassageId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassageId passageId = (PassageId) o;
        return Objects.equals(value, passageId.value);
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
