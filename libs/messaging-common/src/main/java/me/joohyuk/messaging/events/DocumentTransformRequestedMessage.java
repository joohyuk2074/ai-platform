package me.joohyuk.messaging.events;

import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;

/**
 * Document Transform 요청 메시지 (서비스 간 계약).
 *
 * <p>datahub 서비스가 Document Transform을 요청할 때 발행하는 메시지입니다.
 * datarex 서비스는 이 메시지를 소비하여 실제 문서 청킹(chunking) 작업을 수행합니다.
 *
 * <p>이 메시지는 서비스 간 통신 계약을 명확히 정의하며, 도메인 엔티티와 독립적으로 관리됩니다.
 * <p>모든 필드는 primitive 타입 또는 기본 타입으로 구성되어 직렬화가 간편합니다.
 */
public record DocumentTransformRequestedMessage(
    Document document,
    Instant createdAt
) {

  public record Document(
      Long documentId,
      Long collectionId,
      String fileKey,
      String contentHash,
      Metadata metadata,
      String trackingId,
      String status,
      int attempt,
      String lastErrorCode,
      String lastErrorMessage,
      int passageCount,
      String lastResultEventId,
      Instant createdAt,
      Instant updatedAt,
      Long uploader
  ) {

  }
}
