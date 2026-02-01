package me.joohyuk.datahub.domain.port.out.message.publisher;

import com.spartaecommerce.domain.event.publisher.DomainEventPublisher;
import me.joohyuk.datahub.domain.event.PassageCreationRequestedEvent;

/**
 * Passage 생성 요청 이벤트를 Kafka로 발행하는 포트입니다.
 *
 * <p>구현체는 {@link org.springframework.kafka.core.KafkaTemplate}을 직접 사용하여
 * Kafka 토픽으로 이벤트를 produce합니다.
 */
public interface PassageCreationRequestPublisher extends DomainEventPublisher<PassageCreationRequestedEvent> {

  /**
   * Passage 생성 요청 이벤트를 Kafka로 발행합니다.
   *
   * @param event 발행할 Passage 생성 요청 이벤트
   */
  void publish(PassageCreationRequestedEvent event);
}
