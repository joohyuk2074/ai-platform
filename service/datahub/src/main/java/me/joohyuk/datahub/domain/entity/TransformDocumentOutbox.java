package me.joohyuk.datahub.domain.entity;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.domain.vo.DocumentStatus;

/**
 * Transform Document Outbox 엔티티
 * <p>
 * Outbox 패턴을 구현하여 Document Transform 이벤트를 안정적으로 발행합니다.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformDocumentOutbox {

  private Long id;
  private Long sagaId;

  private String type;
  private String payload;

  private SagaStatus sagaStatus;
  @Setter
  private OutboxStatus outboxStatus;

  private DocumentStatus documentStatus;

  private int version;

  private LocalDateTime createdAt;
  private LocalDateTime processedAt;

  /**
   * PENDING 상태의 TransformDocumentOutbox를 생성합니다.
   * <p>
   * @param id             Outbox 레코드 ID
   * @param sagaId         Saga 식별자
   * @param payload        이벤트 JSON 직렬화 문자열
   * @param documentStatus 문서 상태
   * @param sagaStatus     Saga 상태
   * @return PENDING 상태의 새 Outbox 인스턴스
   */
  public static TransformDocumentOutbox createPending(
      Long id,
      Long sagaId,
      String payload,
      DocumentStatus documentStatus,
      SagaStatus sagaStatus
  ) {
    return TransformDocumentOutbox.builder()
        .id(id)
        .sagaId(sagaId)
        .type(DOCUMENT_TRANSFORM_SAGA_NAME)
        .payload(payload)
        .documentStatus(documentStatus)
        .sagaStatus(sagaStatus)
        .outboxStatus(OutboxStatus.PENDING)
        .version(0)
        .createdAt(LocalDateTime.now())
        .processedAt(null)
        .build();
  }
}
