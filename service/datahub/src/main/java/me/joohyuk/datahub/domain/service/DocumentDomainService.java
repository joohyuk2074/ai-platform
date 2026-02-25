package me.joohyuk.datahub.domain.service;

import com.spartaecommerce.domain.vo.DocumentId;
import java.time.Instant;
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
    document.completeTransform(passageCount, eventId, now);

    log.info("Document transformed successfully: documentId={}, passageCount={}",
        document.getId().getValue(), passageCount);

    // TODO: 추후 Passage 처리 이벤트로 변경 필요
//    return TransformDocumentEvent.of(document, now);
    return null;
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
    document.failTransform(errorCode, errorMessage, eventId, now);

    log.warn("Document transform failed: documentId={}, errorCode={}, errorMessage={}",
        document.getId().getValue(), errorCode, errorMessage);
  }

  /**
   * Document Embedding 성공 처리.
   *
   * <p>Document 상태를 EMBED_REQUESTED → EMBEDDED로 전이시킵니다.
   *
   * @param document Document 엔티티
   * @param eventId  수신한 결과 이벤트의 ID (멱등성 체크용)
   * @param now      현재 시간
   */
  public void markEmbedded(
      Document document,
      String eventId,
      Instant now
  ) {
    document.completeEmbed(eventId, now);

    log.info("Document embedded successfully: documentId={}",
        document.getId().getValue());
  }

  /**
   * Document Embedding 실패 처리.
   *
   * <p>Document 상태를 EMBED_REQUESTED → EMBED_FAILED로 전이시킵니다.
   *
   * @param document     Embedding이 실패한 Document
   * @param errorCode    에러 코드
   * @param errorMessage 에러 메시지
   * @param eventId      수신한 이벤트 ID (멱등성 보장용)
   * @param now          현재 시간
   */
  public void cancelEmbed(
      Document document,
      String errorCode,
      String errorMessage,
      String eventId,
      Instant now
  ) {
    document.failEmbed(errorCode, errorMessage, eventId, now);

    log.warn("Document embedding failed: documentId={}, errorCode={}, errorMessage={}",
        document.getId().getValue(), errorCode, errorMessage);
  }
}
