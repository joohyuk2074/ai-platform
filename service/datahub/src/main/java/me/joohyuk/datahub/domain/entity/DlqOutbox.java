package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Dead Letter Queue Outbox 도메인 엔티티
 * <p>
 * DLQ로 전송할 메시지를 Outbox 패턴으로 관리하여 exactly-once 보장
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DlqOutbox {

  private Long id;
  private String correlationId;
  private String originalTopic;
  private String payload;
  private String errorCode;
  private String errorMessage;

  @Setter
  private OutboxStatus outboxStatus;

  private int version;

  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

  /**
   * PENDING 상태의 DlqOutbox를 생성합니다.
   *
   * @param id            Outbox 레코드 ID
   * @param correlationId Saga correlation ID
   * @param originalTopic 원본 토픽 이름
   * @param payload       실패한 메시지의 페이로드
   * @param errorCode     에러 코드
   * @param errorMessage  에러 메시지
   * @param createdAt     생성 시각
   * @return PENDING 상태의 새 DlqOutbox 인스턴스
   */
  public static DlqOutbox createPending(
      Long id,
      String correlationId,
      String originalTopic,
      String payload,
      String errorCode,
      String errorMessage,
      LocalDateTime createdAt
  ) {
    return DlqOutbox.builder()
        .id(id)
        .correlationId(correlationId)
        .originalTopic(originalTopic)
        .payload(payload)
        .errorCode(errorCode)
        .errorMessage(errorMessage)
        .outboxStatus(OutboxStatus.PENDING)
        .version(0)
        .createdAt(createdAt)
        .build();
  }

  /**
   * DLQ 전송이 성공적으로 완료되었음을 기록합니다.
   *
   * @param processedAt 처리 완료 시각
   */
  public void markSent(LocalDateTime processedAt) {
    this.outboxStatus = OutboxStatus.SENT;
    this.processedAt = processedAt;
  }

  /**
   * DLQ 전송이 실패했음을 기록합니다.
   *
   * @param processedAt 처리 시도 시각
   */
  public void markFailed(LocalDateTime processedAt) {
    this.outboxStatus = OutboxStatus.FAILED;
    this.processedAt = processedAt;
  }
}
