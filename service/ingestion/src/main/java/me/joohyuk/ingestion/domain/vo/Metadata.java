package me.joohyuk.ingestion.domain.vo;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 메타데이터 값 객체
 * <p>
 * 문서, 청크, Passage에 대한 부가 정보를 저장합니다. 검색 성능 향상을 위한 필터링 조건으로 활용됩니다.
 * <p>
 * RAG 시스템에서는 메타데이터 필터링을 통해 검색 정확도를 크게 향상시킵니다.
 */
public final class Metadata {

  private final Map<String, Object> attributes;

  private Metadata(Map<String, Object> attributes) {
    this.attributes = new HashMap<>(attributes);
  }

  public static Metadata empty() {
    return new Metadata(Collections.emptyMap());
  }

  public static Metadata of(Map<String, Object> attributes) {
    if (attributes == null) {
      throw new IllegalArgumentException("Attributes cannot be null");
    }
    return new Metadata(attributes);
  }

  /**
   * Builder 패턴으로 메타데이터 생성
   */
  public static Builder builder() {
    return new Builder();
  }

  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public Optional<Object> get(String key) {
    return Optional.ofNullable(attributes.get(key));
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    return get(key)
        .filter(type::isInstance)
        .map(type::cast);
  }

  public boolean containsKey(String key) {
    return attributes.containsKey(key);
  }

  /**
   * 새로운 속성을 추가한 메타데이터 반환 (불변성 유지)
   */
  public Metadata with(String key, Object value) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Metadata key cannot be null or empty");
    }

    Map<String, Object> newAttributes = new HashMap<>(this.attributes);
    newAttributes.put(key, value);
    return new Metadata(newAttributes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return Objects.equals(attributes, metadata.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributes);
  }

  @Override
  public String toString() {
    return "Metadata{" + attributes + '}';
  }

  public static class Builder {

    private final Map<String, Object> attributes = new HashMap<>();

    public Builder put(String key, Object value) {
      if (key != null && !key.isBlank()) {
        attributes.put(key, value);
      }
      return this;
    }

    public Builder fileName(String fileName) {
      return put("fileName", fileName);
    }

    public Builder fileType(String fileType) {
      return put("fileType", fileType);
    }

    public Builder source(String source) {
      return put("source", source);
    }

    public Builder createdAt(Instant createdAt) {
      return put("createdAt", createdAt);
    }

    public Builder author(String author) {
      return put("author", author);
    }

    public Builder tags(String... tags) {
      return put("tags", tags);
    }

    public Metadata build() {
      return new Metadata(attributes);
    }
  }
}
