package me.joohyuk.ingestion.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import java.time.Instant;
import me.joohyuk.ingestion.domain.vo.DocumentId;

/**
 * 문서 검수 실패 이벤트
 * <p>
 * 문서 검수 과정에서 문제가 발견되어 검수를 통과하지 못했을 때 발행됩니다.
 */
public record DocumentValidationFailed(
    DocumentId documentId,
    String reason,
    Instant occurredAt
) implements DomainEvent<DocumentValidationFailed> {

  public static DocumentValidationFailed of(DocumentId documentId, String reason) {
    return new DocumentValidationFailed(documentId, reason, Instant.now());
  }
}
