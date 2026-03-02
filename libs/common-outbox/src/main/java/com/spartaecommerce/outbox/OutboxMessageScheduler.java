package com.spartaecommerce.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아웃박스 테이블에서 {@link OutboxStatus#PENDING} 메시지를 주기적으로 읽어 Kafka로 발행하는
 * 스케줄러입니다.
 *
 * <h3>처리 흐름</h3>
 * <ol>
 *   <li>{@code outbox_messages} 테이블에서 {@code PENDING} 메시지를 생성 시간 순으로 조회합니다.</li>
 *   <li>각 메시지의 {@code payloadJson}을 {@code Map}으로 역직렬화합니다.</li>
 *   <li>{@link KafkaTemplate}을 통해 해당 토픽으로 발행합니다. {@code partition_key}를 메시지 키로 사용하여
 *       동일 키의 이벤트 순서를 보장합니다.</li>
 *   <li>발행 성공 시 {@link OutboxStatus#SENT}로 전이합니다.</li>
 * </ol>
 *
 * <h3>재시도 정책</h3>
 * <ul>
 *   <li>발행 실패 시 {@code retryCount}를 증가시키고 {@code PENDING} 상태를 유지합니다.</li>
 *   <li>{@code retryCount}가 {@link #MAX_RETRY_COUNT}를 초과하면 {@link OutboxStatus#FAILED}로
 *       전이합니다. FAILED 메시지는 수동 조사 또는 별도 DLQ 처리가 필요합니다.</li>
 * </ul>
 *
 * <h3>후속 개선 포인트</h3>
 * <ul>
 *   <li>다중 인스턴스 환경에서 {@code FOR UPDATE SKIP LOCKED} 적용하여 경쟁 조건 방지</li>
 *   <li>배치 크기 제한 (현재는 모든 PENDING 메시지를 한 번에 처리)</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxMessageScheduler {

  /** publish 실패 후 이 횟수를 초과하면 FAILED로 전이합니다. */
  private static final int MAX_RETRY_COUNT = 3;

  private final OutboxMessageJpaRepository outboxRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  /**
   * 1초 간격으로 PENDING 메시지를 폴링하여 Kafka로 발행합니다.
   *
   * <p>{@code fixedDelay}를 사용하여 이전 실행이 완료된 후에만 다음 실행이 시작되므로 자기 자신과의
   * 동시 실행이 발생하지 않습니다. 각 메시지는 독립적으로 처리되어 하나의 발행 실패가 나머지를 차단하지
   * 않습니다.
   */
  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void processOutboxMessages() {
    try {
      List<OutboxMessageJpaEntity> pendingMessages =
          outboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING);

      if (pendingMessages.isEmpty()) {
        return;
      }

      log.debug("Processing {} pending outbox messages", pendingMessages.size());

      for (OutboxMessageJpaEntity message : pendingMessages) {
        publishMessage(message);
      }
    } catch (Exception e) {
      // 스케줄러 자체가 죽지 않도록 최상위 예외를 캡처합니다.
      // 개별 메시지 처리 실패는 publishMessage 내부에서 처리됩니다.
      log.error("Unexpected error while processing outbox messages", e);
    }
  }

  /**
   * 단일 아웃박스 메시지를 Kafka로 발행합니다.
   *
   * <p>{@link KafkaTemplate#send}는 비동기적으로 동작하므로, {@code get()}을 호출하여 동기적으로
   * 결과를 확인합니다. 발행 성공 시 {@link OutboxStatus#SENT}로, 실패 시 재시도 카운터를
   * 증가시킵니다.
   */
  private void publishMessage(OutboxMessageJpaEntity message) {
    try {
      Map<String, Object> payload = deserializePayload(message.getPayloadJson());
      kafkaTemplate.send(message.getTopic(), message.getPartitionKey(), payload).get();

      message.markSent(Instant.now());
      log.info("Outbox message sent successfully: id={}, topic={}, partitionKey={}",
          message.getId(), message.getTopic(), message.getPartitionKey());

    } catch (Exception e) {
      handlePublishFailure(message, e);
    }
  }

  /**
   * publish 실패를 처리합니다. 재시도 횟수를 증가시키고, 최대 재시도를 초과하면 FAILED로 전이합니다.
   */
  private void handlePublishFailure(OutboxMessageJpaEntity message, Exception e) {
    message.incrementRetry(e.getMessage());
    log.warn("Failed to publish outbox message: id={}, retryCount={}, error={}",
        message.getId(), message.getRetryCount(), e.getMessage());

    if (message.getRetryCount() > MAX_RETRY_COUNT) {
      message.markFailed(e.getMessage());
      log.error("Outbox message exceeded max retry count ({}), marked as FAILED: id={}",
          MAX_RETRY_COUNT, message.getId());
    }
  }

  /**
   * 아웃박스에 저장된 JSON 페이로드를 {@code Map}으로 역직렬화합니다.
   *
   * <p>이벤트 타입에 무관하게 {@code Map}으로 복원한 후 {@link KafkaTemplate}의 {@code JsonSerializer}가
   * 다시 직렬화하여 Consumer 측에서 원하는 타입으로 역직렬화할 수 있게 됩니다.
   */
  private Map<String, Object> deserializePayload(String payloadJson) {
    try {
      return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize outbox payload", e);
    }
  }
}
