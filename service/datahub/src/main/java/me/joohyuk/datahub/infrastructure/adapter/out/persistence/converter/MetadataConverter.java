package me.joohyuk.datahub.infrastructure.adapter.out.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.vo.Metadata;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class MetadataConverter implements AttributeConverter<Metadata, String> {

  private final ObjectMapper objectMapper;

  @Override
  public String convertToDatabaseColumn(Metadata metadata) {
    if (metadata == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize Metadata to JSON", e);
    }
  }

  @Override
  public Metadata convertToEntityAttribute(String json) {
    if (json == null || json.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, Metadata.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize Metadata from JSON", e);
    }
  }
}
