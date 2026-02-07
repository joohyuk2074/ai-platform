package me.joohyuk.datahub.domain.service;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.util.DateTimeHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentDomainService {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;

  public DocumentUploadedEvent initializeDocument(Document document) {
    document.initialize(idGenerator.generateId(), dateTimeHolder.now());

    log.info("Document created with documentId: {} and fileKey: {}", document.getId().getValue(),
        document.getFileKey());

    return new DocumentUploadedEvent(document, dateTimeHolder.getCurrentDateTime());
  }

  /**
   * Document Transform 완료 처리.
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORMED로 전이시킵니다.
   *
   * @param document Transform이 완료된 Document
   * @param passageCount 생성된 passage 수
   * @param eventId 수신한 이벤트 ID (멱등성 보장용)
   * @return PassageCreationRequestEvent (추후 다음 단계에서 사용)
   */
  public PassageCreationRequestEvent createPassage(Document document, int passageCount, String eventId) {
    document.markPassageCreated(passageCount, eventId, dateTimeHolder.now());

    log.info("Document transformed successfully: documentId={}, passageCount={}",
        document.getId().getValue(), passageCount);

    // TODO: 추후 Passage 처리 이벤트로 변경 필요
    return new PassageCreationRequestEvent(document, dateTimeHolder.getCurrentDateTime());
  }

  /**
   * Document Transform 실패 처리.
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORM_FAILED로 전이시킵니다.
   *
   * @param document Transform이 실패한 Document
   * @param errorCode 에러 코드
   * @param errorMessage 에러 메시지
   * @param eventId 수신한 이벤트 ID (멱등성 보장용)
   */
  public void cancelCreatePassage(Document document, String errorCode, String errorMessage, String eventId) {
    document.markPassageFailed(errorCode, errorMessage, eventId, dateTimeHolder.now());

    log.warn("Document transform failed: documentId={}, errorCode={}, errorMessage={}",
        document.getId().getValue(), errorCode, errorMessage);
  }
}
