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

  private final Long sagaId;

  private final Long documentId;
  private final Long collectionId;
  private final String fileKey;
  private final String contentHash;
  private final Metadata metadata;
  private final String trackingId;
  private final String status;
  private final int attempt;
  private final String lastErrorCode;
  private final String lastErrorMessage;
  private final int passageCount;
  private final String lastResultEventId;
  private final Instant documentCreatedAt;
  private final Instant documentUpdatedAt;
  private final Instant eventCreatedAt;
  private final Long uploadedBy;

  public static TransformDocumentEvent from(Long sagaId, Document document) {
    return TransformDocumentEvent.builder()
        .sagaId(sagaId)
        .documentId(document.getId().getValue())
        .collectionId(document.getCollectionId().getValue())
        .fileKey(document.getFileKey())
        .contentHash(document.getContentHash().getValue())
        .metadata(document.getMetadata())
        .trackingId(document.getTrackingId().getValue().toString())
        .status(document.getStatus().name())
        .attempt(document.getAttempt())
        .lastErrorCode(document.getLastErrorCode())
        .lastErrorMessage(document.getLastErrorMessage())
        .passageCount(document.getPassageCount())
        .lastResultEventId(document.getLastResultEventId())
        .documentCreatedAt(document.getCreatedAt())
        .documentUpdatedAt(document.getUpdatedAt())
        .eventCreatedAt(document.getCreatedAt())
        .uploadedBy(document.getUploader().getValue())
        .build();
  }
}
