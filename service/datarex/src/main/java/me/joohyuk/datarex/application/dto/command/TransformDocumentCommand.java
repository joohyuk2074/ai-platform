package me.joohyuk.datarex.application.dto.command;

import com.spartaecommerce.domain.vo.Metadata;
import me.joohyuk.datarex.infrastructure.adapter.in.message.dto.TransformDocumentEventMessage;

public record TransformDocumentCommand(
    Long sagaId,
    Long documentId,
    Long collectionId,
    String fileKey,
    String contentHash,
    Metadata metadata,
    int attempt
) {

  /**
   * TransformDocumentEventMessage로부터 TransformDocumentCommand를 생성합니다.
   *
   * @param event Kafka로부터 수신한 이벤트 메시지
   * @return 변환된 Command 객체
   */
  public static TransformDocumentCommand of(TransformDocumentEventMessage event) {
    return new TransformDocumentCommand(
        event.getSagaId(),
        event.getDocumentId(),
        event.getCollectionId(),
        event.getFileKey(),
        event.getContentHash(),
        event.getMetadata(),
        event.getAttempt()
    );
  }
}
