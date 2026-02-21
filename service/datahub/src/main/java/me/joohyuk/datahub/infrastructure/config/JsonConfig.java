package me.joohyuk.datahub.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.JsonSerializer;
import com.spartaecommerce.infrastructure.json.ObjectMapperJsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON 직렬화/역직렬화 관련 설정
 */
@Configuration
public class JsonConfig {

  @Bean
  public JsonSerializer jsonSerializer(ObjectMapper objectMapper) {
    return new ObjectMapperJsonSerializer(objectMapper);
  }
}
