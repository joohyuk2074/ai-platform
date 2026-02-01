package com.spartaecommerce.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "outbox_messages",
    indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessageJpaEntity {

  /**
   * AUTO_INCREMENT 기반 PK. 아웃박스는 서비스 내부 기구이므로 Snowflake 불필요.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "topic", nullable = false)
  private String topic;

  /**
   * Kafka 메시지 키 (파티셔닝 키). 동일 키의 이벤트 순서를 보장합니다.
   */
  @Column(name = "partition_key", nullable = false)
  private String partitionKey;

  /**
   * Jackson으로 직렬화된 이벤트 페이로드 (JSON 문자열).
   */
  @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
  private String payloadJson;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxMessageStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**
   * Kafka publish 성공 시점. {@link OutboxMessageStatus#SENT} 상태로 전이될 때 채워집니다.
   */
  @Column(name = "sent_at")
  private Instant sentAt;

  /**
   * 현재까지의 publish 재시도 횟수. 초기값 0.
   */
  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  /**
   * 마지막 publish 실패 시의 오류 메시지 (최대 500자).
   */
  @Column(name = "last_error", length = 500)
  private String lastError;

  /**
   * 새 아웃박스 메시지를 {@link OutboxMessageStatus#PENDING} 상태로 생성합니다.
   *
   * @param topic        목적지 Kafka 토픽
   * @param partitionKey 파티셔닝 키
   * @param payloadJson  직렬화된 이벤트 페이로드
   * @param createdAt    생성 시점
   */
  public OutboxMessageJpaEntity(
      String topic,
      String partitionKey,
      String payloadJson,
      Instant createdAt
  ) {
    this.topic = topic;
    this.partitionKey = partitionKey;
    this.payloadJson = payloadJson;
    this.status = OutboxMessageStatus.PENDING;
    this.createdAt = createdAt;
    this.retryCount = 0;
  }

  /**
   * Kafka publish 성공 시 {@link OutboxMessageStatus#SENT}로 전이합니다.
   *
   * @param sentAt 전송 완료 시점
   */
  public void markSent(Instant sentAt) {
    this.status = OutboxMessageStatus.SENT;
    this.sentAt = sentAt;
    this.lastError = null;
  }

  /**
   * publish 실패 시 재시도 횟수를 증가시키고 오류 정보를 저장합니다.
   * 상태는 {@link OutboxMessageStatus#PENDING}으로 유지되어 다음 폴링에서 재시도됩니다.
   *
   * @param error 오류 메시지
   */
  public void incrementRetry(String error) {
    this.retryCount++;
    this.lastError = truncate(error);
  }

  /**
   * 최대 재시도 횟수를 초과하면 {@link OutboxMessageStatus#FAILED}로 전이합니다.
   *
   * @param error 마지막 오류 메시지
   */
  public void markFailed(String error) {
    this.status = OutboxMessageStatus.FAILED;
    this.lastError = truncate(error);
  }

  private static final int MAX_ERROR_LENGTH = 500;

  private static String truncate(String message) {
    if (message == null) {
      return null;
    }
    return message.length() > MAX_ERROR_LENGTH
        ? message.substring(0, MAX_ERROR_LENGTH)
        : message;
  }
}
