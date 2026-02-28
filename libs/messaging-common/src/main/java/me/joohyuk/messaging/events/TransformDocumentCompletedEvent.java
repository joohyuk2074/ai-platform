package me.joohyuk.messaging.events;

import com.spartaecommerce.domain.vo.TrackingId;
import java.time.Instant;

public record TransformDocumentCompletedEvent(
    String correlationId,
    TrackingId trackingId,
    String collectionId,
    String documentId,
    String contentHash,
    int passageCount,
    String errorCode,
    String errorMessage,
    Instant occurredAt
) {

  public static TransformDocumentCompletedEvent success(
      String correlationId,
      TrackingId trackingId,
      String collectionId,
      String documentId,
      String passageVersion,
      int passageCount,
      Instant occurredAt
  ) {
    return new TransformDocumentCompletedEvent(
        correlationId,
        trackingId,
        collectionId,
        documentId,
        passageVersion,
        passageCount,
        null,
        null,
        occurredAt
    );
  }

  public static TransformDocumentCompletedEvent failure(
      String correlationId,
      TrackingId trackingId,
      String collectionId,
      String documentId,
      String contentHash,
      String errorCode,
      String errorMessage,
      Instant occurredAt
  ) {
    return new TransformDocumentCompletedEvent(
        correlationId,
        trackingId,
        collectionId,
        documentId,
        contentHash,
        0,
        errorCode,
        errorMessage,
        occurredAt
    );
  }

  public boolean isSuccess() {
    return errorCode == null;
  }
}
