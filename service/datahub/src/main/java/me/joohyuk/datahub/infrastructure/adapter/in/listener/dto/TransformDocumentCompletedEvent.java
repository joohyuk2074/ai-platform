package me.joohyuk.datahub.infrastructure.adapter.in.listener.dto;

import java.time.Instant;

public record TransformDocumentCompletedEvent(
    String eventId,
    Long sagaId,
    String collectionId,
    String documentId,
    String contentHash,
    int passageCount,
    String errorCode,
    String errorMessage,
    Instant occurredAt
) {

  public boolean transformCompleted() {
    return this.errorCode == null || errorCode.isBlank();
  }
}
