package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult;
import me.joohyuk.datahub.application.dto.result.TransformDocumentResult.DocumentRequestResult;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationRequestCommandHandler {

  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final PassageCreationRequestPublisher passageCreationRequestPublisher;
  private final DateTimeHolder dateTimeHolder;

  public TransformDocumentResult requestPassageCreationByCollection(
      CollectionId collectionId) {
    if (!documentCollectionRepository.existsById(collectionId)) {
      throw new DatahubDomainException(
          "Failed to find Collection. collectionId: " + collectionId.getValue());
    }

    List<Document> documents =
        documentRepository.findByCollectionId(collectionId, DocumentStatus.UPLOADED);

    Instant now = dateTimeHolder.now();
    List<DocumentRequestResult> documentResults = new ArrayList<>();
    int successCount = 0;

    for (Document document : documents) {
      try {
        // TODO: outbox 패턴 적용
        document.transform(now);
        documentRepository.save(document);

        PassageCreationRequestEvent event =
            new PassageCreationRequestEvent(document, dateTimeHolder.getCurrentDateTime());
        passageCreationRequestPublisher.publish(event);

        documentResults.add(DocumentRequestResult.success(document.getId()));
        successCount++;

        log.debug("Passage creation request event published for documentId={}",
            document.getId().getValue());
      } catch (Exception e) {
        log.error("Failed to publish passage creation request event for documentId={}",
            document.getId().getValue(), e);
        documentResults.add(DocumentRequestResult.failure(document.getId(), e.getMessage()));
      }
    }

    TransformDocumentResult result = new TransformDocumentResult(
        collectionId,
        documents.size(),
        successCount,
        now,
        documentResults
    );

    log.info(
        "Passage creation request events published for collectionId={}: total={}, published={}, failed={}",
        collectionId.getValue(), result.totalDocumentsFound(), result.successfullyRequested(),
        result.totalDocumentsFound() - result.successfullyRequested());

    return result;
  }
}
