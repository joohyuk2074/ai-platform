package me.joohyuk.datarex.infrastructure.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Value("${spring.kafka.consumer.auto-offset-reset}")
  private String autoOffsetReset;

  @Value("${spring.kafka.consumer.enable-auto-commit}")
  private boolean enableAutoCommit;

  @Value("${spring.kafka.consumer.key-deserializer}")
  private String keyDeserializer;

  @Value("${spring.kafka.consumer.value-deserializer}")
  private String valueDeserializer;

  @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
  private String trustedPackages;

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

    // JsonDeserializer를 위한 trusted packages 설정
    configProps.put("spring.json.trusted.packages", trustedPackages);

    // Type Mapping: 공통 메시징 DTO를 사용하므로 불필요 (동일한 클래스 사용)
    // datahub와 datarex 모두 me.joohyuk.messaging.events.DocumentTransformRequestedMessage 사용

    return new DefaultKafkaConsumerFactory<>(configProps);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    // 수동 커밋 모드 설정 (enable-auto-commit: false이므로)
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

    return factory;
  }

  /**
   * 개발 환경: 애플리케이션 시작 시 consumer group을 삭제하여 offset 초기화
   * Consumer group이 삭제되면 auto-offset-reset: earliest 설정에 의해 처음부터 메시지를 읽게 됩니다.
   */
  @Bean
  public ApplicationRunner resetConsumerGroupOnStartup() {
    return args -> {
      Map<String, Object> adminProps = new HashMap<>();
      adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

      try (AdminClient adminClient = AdminClient.create(adminProps)) {
        try {
          // Consumer group 삭제
          adminClient.deleteConsumerGroups(Collections.singletonList(groupId)).all().get();
          log.info("개발 환경: Consumer group '{}' 삭제 완료 (offset 초기화)", groupId);
        } catch (ExecutionException e) {
          // Consumer group이 존재하지 않는 경우는 정상 (첫 실행)
          if (e.getCause() instanceof org.apache.kafka.common.errors.GroupIdNotFoundException) {
            log.info("개발 환경: Consumer group '{}'이 존재하지 않음 (첫 실행)", groupId);
          } else {
            log.warn("개발 환경: Consumer group '{}' 삭제 중 오류 발생: {}", groupId,
                e.getMessage());
          }
        }
      } catch (Exception e) {
        log.error("개발 환경: Kafka Admin Client 생성 또는 사용 중 오류 발생", e);
      }
    };
  }
}
