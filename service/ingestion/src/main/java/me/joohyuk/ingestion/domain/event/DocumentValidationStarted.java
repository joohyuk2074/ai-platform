package me.joohyuk.ingestion.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import java.time.Instant;
import me.joohyuk.ingestion.domain.vo.DocumentId;

/**
 * 문서 검수 시작 이벤트
 * <p>
 * 문서 검수 프로세스가 시작되었을 때 발행됩니다.
 */
public record DocumentValidationStarted(
    DocumentId documentId,
    Instant occurredAt
) implements DomainEvent<DocumentValidationStarted> {

  public static DocumentValidationStarted of(DocumentId documentId) {
    return new DocumentValidationStarted(documentId, Instant.now());
  }
}
