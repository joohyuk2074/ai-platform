package me.joohyuk.vecdash.domain.vo;

import java.util.Objects;

/**
 * 콘텐츠 값 객체
 *
 * 문서, 청크, Passage의 텍스트 내용을 표현합니다.
 * 비즈니스 규칙을 통해 유효성을 검증합니다.
 */
public final class Content {

    private static final int MAX_CONTENT_LENGTH = 1_000_000; // 1MB 텍스트 제한

    private final String text;
    private final int length;

    private Content(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Content text cannot be null");
        }

        // 빈 문서는 허용 (초기 상태나 삭제된 내용을 위해)
        if (text.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                "Content length exceeds maximum allowed length: " + MAX_CONTENT_LENGTH
            );
        }

        this.text = text;
        this.length = text.length();
    }

    public static Content of(String text) {
        return new Content(text);
    }

    public static Content empty() {
        return new Content("");
    }

    public String getText() {
        return text;
    }

    public int getLength() {
        return length;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    /**
     * 콘텐츠가 청킹에 적합한지 검증
     */
    public boolean isChunkable() {
        return !isEmpty() && length > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(text, content.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        if (text.length() > 100) {
            return text.substring(0, 100) + "... (length: " + length + ")";
        }
        return text;
    }
}
