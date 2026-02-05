package com.spartaecommerce.domain.vo;

import java.util.Objects;

/**
 * 파일 콘텐츠의 SHA-256 해시값을 나타내는 값 타입입니다.
 *
 * <p>중복 파일 검증의 핵심 식별자로 사용되며, 생성 시 유효한 SHA-256 hex 문자열(64자)인지
 * 검증합니다. 해시 계산의 구현 세부사항(알고리즘 선택, DigestInputStream 패턴 등)은
 * {@code infrastructure.util.ContentHasher}에 위임됩니다.
 */
public final class ContentHash {

  private static final int SHA256_HEX_LENGTH = 64;

  private final String value;

  private ContentHash(String value) {
    validate(value);
    this.value = value;
  }

  public static ContentHash of(String value) {
    return new ContentHash(value);
  }

  private static void validate(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ContentHash cannot be empty");
    }
    if (value.length() != SHA256_HEX_LENGTH) {
      throw new IllegalArgumentException(
          "ContentHash must be a valid SHA-256 hex string ("
              + SHA256_HEX_LENGTH + " characters), but was: " + value.length());
    }
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ContentHash that)) {
      return false;
    }
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
