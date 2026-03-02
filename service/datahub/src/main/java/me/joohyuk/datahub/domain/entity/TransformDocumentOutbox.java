package me.joohyuk.datahub.domain.entity;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformDocumentOutbox {

  private Long id;
  private String correlationId;

  private String type;
  private String payload;

  @Setter
  private OutboxStatus outboxStatus;

  private DocumentStatus documentStatus;

  private int version;

  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

  /**
   * PENDING 상태의 TransformDocumentOutbox를 생성합니다.
   * <p>
   *
   * @param id             Outbox 레코드 ID
   * @param payload        이벤트 JSON 직렬화 문자열
   * @param documentStatus 문서 상태
   * @return PENDING 상태의 새 Outbox 인스턴스
   */
  public static TransformDocumentOutbox createPending(
      Long id,
      String correlationId,
      String payload,
      DocumentStatus documentStatus,
      LocalDateTime createdAt
  ) {
    return TransformDocumentOutbox.builder()
        .id(id)
        .correlationId(correlationId)
        .type(DOCUMENT_TRANSFORM_SAGA_NAME)
        .payload(payload)
        .outboxStatus(OutboxStatus.PENDING)
        .documentStatus(documentStatus)
        .version(0)
        .createdAt(createdAt)
        .build();
  }

  /**
   * 문서 변환이 성공적으로 완료되었음을 기록합니다.
   * Outbox 상태는 이미 SENT이므로 변경하지 않고, processedAt만 기록합니다.
   * (SENT + processedAt != null = 성공적으로 완료된 상태)
   *
   * @param processedAt 처리 완료 시각
   */
  public void markCompleted(LocalDateTime processedAt) {
    this.processedAt = processedAt;
  }

  /**
   * Outbox를 실패 상태로 마킹합니다. (SENT -> FAILED)
   *
   * @param processedAt 처리 시도 시각
   */
  public void markFailed(LocalDateTime processedAt) {
    this.outboxStatus = OutboxStatus.FAILED;
    this.processedAt = processedAt;
  }

  /**
   * 재시도 가능한 오류 발생 시 Outbox를 PENDING 상태로 되돌립니다.
   * 스케줄러가 다시 이 Outbox를 처리하여 재시도할 수 있도록 합니다.
   */
  public void markForRetry() {
    this.outboxStatus = OutboxStatus.PENDING;
  }
}
