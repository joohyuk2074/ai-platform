package me.joohyuk.messaging.events;

import java.time.Instant;

/**
 * Document Transform 결과 메시지 (서비스 간 계약).
 *
 * <p>datarex 서비스가 문서 청킹 작업을 완료(성공/실패)했을 때 발행하는 메시지입니다.
 * datahub 서비스는 이 메시지를 소비하여 Document 엔티티의 상태를 업데이트합니다.
 *
 * <p>{@link #success}와 {@link #failure} 정적 팩터리 메서드로 생성하세요.
 */
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

  public static TransformDocumentCompletedEvent success(
      String eventId,
      Long sagaId,
      String collectionId,
      String documentId,
      String passageVersion,
      int passageCount,
      Instant occurredAt
  ) {
    return new TransformDocumentCompletedEvent(
        eventId,
        sagaId,
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
      String eventId,
      Long sagaId,
      String collectionId,
      String documentId,
      String contentHash,
      String errorCode,
      String errorMessage,
      Instant occurredAt
  ) {
    return new TransformDocumentCompletedEvent(
        eventId,
        sagaId,
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
