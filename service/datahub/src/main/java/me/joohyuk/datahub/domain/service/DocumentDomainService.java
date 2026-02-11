package me.joohyuk.datahub.domain.service;

import com.spartaecommerce.domain.vo.DocumentId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentDomainService {

  public DocumentUploadedEvent upload(
      DocumentId documentId,
      Document document,
      Instant now
  ) {
    document.initialize(documentId, now);

    log.info("Document created with documentId: {} and fileKey: {}",
        document.getId().getValue(),
        document.getFileKey()
    );

    return new DocumentUploadedEvent(document, now);
  }

  public TransformDocumentEvent transform(
      Document document,
      int passageCount,
      String eventId,
      Instant now
  ) {
    document.markPassageCreated(passageCount, eventId, now);

    log.info("Document transformed successfully: documentId={}, passageCount={}",
        document.getId().getValue(), passageCount);

    // TODO: 추후 Passage 처리 이벤트로 변경 필요
    return TransformDocumentEvent.of(document, now);
  }

  /**
   * Document Transform 실패 처리.
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORM_FAILED로 전이시킵니다.
   *
   * @param document     Transform이 실패한 Document
   * @param errorCode    에러 코드
   * @param errorMessage 에러 메시지
   * @param eventId      수신한 이벤트 ID (멱등성 보장용)
   */
  public void cancelCreatePassage(
      Document document,
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    document.markPassageFailed(errorCode, errorMessage, eventId, now);

    log.warn("Document transform failed: documentId={}, errorCode={}, errorMessage={}",
        document.getId().getValue(), errorCode, errorMessage);
  }
}
