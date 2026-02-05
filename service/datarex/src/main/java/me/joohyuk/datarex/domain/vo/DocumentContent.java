package me.joohyuk.datarex.domain.vo;

import java.util.HashMap;
import java.util.Map;

public record DocumentContent(
    String content,
    Map<String, Object> metadata
) {

  public DocumentContent(String content, Map<String, Object> metadata) {
    this.content = content;
    this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
  }

  public DocumentContent(String content) {
    this(content, new HashMap<>());
  }

  @Override
  public Map<String, Object> metadata() {
    return new HashMap<>(metadata);
  }

  public Object getMetadata(String key) {
    return metadata.get(key);
  }

  public DocumentContent withMetadata(String key, Object value) {
    Map<String, Object> newMetadata = new HashMap<>(this.metadata);
    newMetadata.put(key, value);
    return new DocumentContent(this.content, newMetadata);
  }

  public DocumentContent withMetadata(Map<String, Object> additionalMetadata) {
    Map<String, Object> newMetadata = new HashMap<>(this.metadata);
    newMetadata.putAll(additionalMetadata);
    return new DocumentContent(this.content, newMetadata);
  }
}