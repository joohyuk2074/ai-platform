package me.joohyuk.ingestion.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import java.time.Instant;
import me.joohyuk.ingestion.domain.vo.DocumentId;
import me.joohyuk.ingestion.domain.vo.Metadata;

/**
 * 문서 업로드 완료 이벤트
 * <p>
 * 새로운 문서가 시스템에 업로드되었을 때 발행됩니다.
 */
public record DocumentUploaded(
    DocumentId documentId,
    String fileName,
    int contentLength,
    Metadata metadata,
    Instant occurredAt
) implements DomainEvent<DocumentUploaded> {

  public static DocumentUploaded of(
      DocumentId documentId,
      String fileName,
      int contentLength,
      Metadata metadata
  ) {
    return new DocumentUploaded(
        documentId,
        fileName,
        contentLength,
        metadata,
        Instant.now()
    );
  }
}
