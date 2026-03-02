package me.joohyuk.datahub.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.topic")
public class KafkaTopicProperties {

  /**
   * Topic for transform document events
   */
  private String transformDocument;

  /**
   * Topic for passage creation requests
   */
  private String passageCreation;

  /**
   * Topic for document transform results
   */
  private String documentTransformResult;
}
