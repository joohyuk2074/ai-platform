package com.spartaecommerce.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이벤트를 아웃박스 테이블에 저장하는 공통 헬퍼입니다.
 *
 * <p>호출 시점의 <b>기존 트랜잭션에 참여</b>하므로, 도메인 엔티티 저장과 아웃박스 레코드가
 * 원자적으로 커밋됩니다. 실제 Kafka publish는 {@link OutboxMessageScheduler}가 독립적으로 수행합니다.
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @Component
 * public class MyEventPublisher implements MyEventPort {
 *     private final OutboxMessagePublisher outboxPublisher;
 *
 *     @Override
 *     public void publish(MyEvent event) {
 *         outboxPublisher.publish("my.topic", event.id(), event, event.timestamp());
 *     }
 * }
 * }</pre>
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxMessagePublisher {

  private final OutboxMessageJpaRepository outboxRepository;
  private final ObjectMapper objectMapper;

  /**
   * 이벤트를 직렬화하여 아웃박스 테이블에 저장합니다.
   *
   * @param topic        목적지 Kafka 토픽
   * @param partitionKey 파티셔닝 키 (동일 키의 이벤트 순서 보장)
   * @param event        직렬화할 이벤트 객체
   * @param createdAt    이벤트 생성 시점
   * @throws RuntimeException 이벤트 직렬화에 실패한 경우
   */
  public void publish(String topic, String partitionKey, Object event, Instant createdAt) {
    String payloadJson = serializeEvent(event);

    OutboxMessageJpaEntity outboxMessage = new OutboxMessageJpaEntity(
        topic,
        partitionKey,
        payloadJson,
        createdAt
    );

    outboxRepository.save(outboxMessage);
    log.info("Outbox message saved: topic={}, partitionKey={}", topic, partitionKey);
  }

  private String serializeEvent(Object event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize outbox event", e);
    }
  }
}
