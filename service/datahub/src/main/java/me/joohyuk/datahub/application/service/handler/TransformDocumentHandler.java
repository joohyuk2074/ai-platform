package me.joohyuk.datahub.application.service.handler;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.application.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentHandler {

  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final DateTimeHolder dateTimeHolder;
  private final IdGenerator idGenerator;

  public List<TransformDocumentEvent> processTransformRequest(CollectionId collectionId) {
    List<Document> documents = fetchValidatedDocuments(collectionId);

    if (documents.isEmpty()) {
      log.info("No documents to transform for collectionId: {}", collectionId.getValue());
      return List.of();
    }

    Instant now = dateTimeHolder.now();
    documents.forEach(document -> document.transform(now));

    documentRepository.saveAll(documents);
    log.info("Batch saved {} documents for transform", documents.size());

    List<TransformDocumentEvent> events = documents.stream()
        .map(document -> {
          Long sagaId = idGenerator.generateId();
          return TransformDocumentEvent.from(sagaId, document);
        })
        .toList();

    log.debug("Processed {} documents for transform - collectionId: {}",
        events.size(), collectionId.getValue());

    return events;
  }

  public void complete(DocumentId documentId, Integer passageCount, String eventId) {
    Document document = documentRepository.getById(documentId);
    document.completeTransform(passageCount, eventId, dateTimeHolder.now());
    documentRepository.save(document);
  }

  public void failed(DocumentId documentId, String errorCode, String errorMessage, String eventId) {
    Document document = documentRepository.getById(documentId);
    document.failTransform(errorCode, errorMessage, eventId, dateTimeHolder.now());
    documentRepository.save(document);
  }

  private List<Document> fetchValidatedDocuments(CollectionId collectionId) {
    if (!documentCollectionRepository.existsById(collectionId)) {
      throw new DatahubDomainException(
          "Failed to find Collection. collectionId: " + collectionId.getValue(),
          DatahubErrorCode.DOCUMENT_COLLECTION_NOT_FOUND
      );
    }

    List<Document> documents =
        documentRepository.findByCollectionId(collectionId, DocumentStatus.UPLOADED);

    log.debug("Fetched {} documents for transform - collectionId: {}",
        documents.size(), collectionId.getValue());

    return documents;
  }
}
