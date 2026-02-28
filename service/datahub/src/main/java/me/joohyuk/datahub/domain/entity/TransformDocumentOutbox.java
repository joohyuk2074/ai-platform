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
}
