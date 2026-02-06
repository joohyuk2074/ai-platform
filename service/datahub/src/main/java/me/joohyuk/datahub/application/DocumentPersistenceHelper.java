package me.joohyuk.datahub.application;

import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.service.DocumentDomainService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentPersistenceHelper {

  private final DocumentDomainService documentDomainService;
  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final PassageCreationRequestPublisher passageCreationRequestPublisher;
  private final DateTimeHolder dateTimeHolder;

  @Transactional
  public DocumentUploadedEvent persistDocument(Document document) {
    if (!documentCollectionRepository.existsById(document.getCollectionId())) {
      throw new IngestionDomainException(
          "Failed to find Collection. collectionId: " + document.getCollectionId().getValue());
    }

    // 1. Document ID 초기화 및 DocumentUploadedEvent 생성
    DocumentUploadedEvent uploadEvent = documentDomainService.initializeDocument(document);

    // 2. UPLOADED → TRANSFORM_REQUESTED 상태 전이
    Instant now = dateTimeHolder.now();
    document.transform(now);

    // 3. Document 저장 (TRANSFORM_REQUESTED 상태)
    Document savedDocument = documentRepository.save(document);
    log.info("Document saved: documentId={}, status={}",
        savedDocument.getId().getValue(), savedDocument.getStatus());

    // 4. Transform 요청 이벤트를 Kafka로 발행
    PassageCreationRequestEvent passageEvent =
        new PassageCreationRequestEvent(document, dateTimeHolder.getCurrentDateTime());
    passageCreationRequestPublisher.publish(passageEvent);

    return uploadEvent;
  }
}
