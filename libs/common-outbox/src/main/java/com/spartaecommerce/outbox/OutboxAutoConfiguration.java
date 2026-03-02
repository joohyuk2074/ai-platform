package com.spartaecommerce.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 아웃박스 패턴 기반 메시지 발행을 위한 자동 설정입니다.
 *
 * <p>JPA {@link DataSource}가 클래스패스와 컨텍스트에 존재할 때만 활성화됩니다.
 * {@link OutboxMessageScheduler}는 {@link KafkaTemplate} 빈이 존재할 때만 등록됩니다.
 */
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnBean(DataSource.class)
@EnableJpaRepositories(basePackages = "com.spartaecommerce.outbox")
public class OutboxAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public OutboxMessagePublisher outboxMessagePublisher(
      OutboxMessageJpaRepository repository,
      ObjectMapper objectMapper
  ) {
    return new OutboxMessagePublisher(repository, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(KafkaTemplate.class)
  public OutboxMessageScheduler outboxMessageScheduler(
      OutboxMessageJpaRepository repository,
      KafkaTemplate<String, Object> kafkaTemplate,
      ObjectMapper objectMapper
  ) {
    return new OutboxMessageScheduler(repository, kafkaTemplate, objectMapper);
  }
}
