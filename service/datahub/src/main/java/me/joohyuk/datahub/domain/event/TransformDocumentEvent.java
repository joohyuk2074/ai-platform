package me.joohyuk.datahub.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import me.joohyuk.datahub.domain.entity.Document;

@Getter
@Builder(toBuilder = true)
public class TransformDocumentEvent implements DomainEvent<Document> {

  private final String trackingId;
  private final String correlationId;

  private final Long collectionId;
  private final Long documentId;

  private final String documentStatus;
  private final String fileKey;
  private final String contentHash;
  private final Metadata metadata;

  private final int attempt;
  private final Instant eventCreatedAt;

  public static TransformDocumentEvent from(
      String correlationId,
      Document document,
      Instant eventCreatedAt
  ) {
    return TransformDocumentEvent.builder()
        .trackingId(document.getTrackingId().getValue().toString())
        .correlationId(correlationId)
        .collectionId(document.getCollectionId().getValue())
        .documentId(document.getId().getValue())
        .documentStatus(document.getStatus().name())
        .fileKey(document.getFileKey())
        .contentHash(document.getContentHash().getValue())
        .metadata(document.getMetadata())
        .attempt(document.getAttempt())
        .eventCreatedAt(eventCreatedAt)
        .build();
  }
}
