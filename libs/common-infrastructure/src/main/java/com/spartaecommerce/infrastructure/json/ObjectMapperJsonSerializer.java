package com.spartaecommerce.infrastructure.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.JsonSerializationException;
import com.spartaecommerce.domain.port.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Jackson ObjectMapper를 사용한 JsonSerializer 구현체
 * <p>
 * 헥사고날 아키텍처의 Adapter로서, Jackson 라이브러리를 사용하여 JSON 직렬화/역직렬화를 수행합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ObjectMapperJsonSerializer implements JsonSerializer {

  private final ObjectMapper objectMapper;

  @Override
  public <T> String serialize(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize object to JSON: {}", object.getClass().getName(), e);
      throw new JsonSerializationException(
          "Failed to serialize object to JSON: " + object.getClass().getName(),
          e
      );
    }
  }

  @Override
  public <T> T deserialize(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize JSON to {}: {}", clazz.getName(), json, e);
      throw new JsonSerializationException(
          "Failed to deserialize JSON to " + clazz.getName(),
          e
      );
    }
  }
}
