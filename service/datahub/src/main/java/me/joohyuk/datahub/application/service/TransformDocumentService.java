package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;
import me.joohyuk.datahub.application.port.in.service.TransformDocumentUseCase;
import me.joohyuk.datahub.application.service.handler.DocumentPersistenceHandler;
import me.joohyuk.datahub.application.service.handler.DocumentTransformHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentSagaHandler;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentEvent;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentService implements TransformDocumentUseCase {

  private final DocumentTransformHandler documentTransformHandler;
  private final DocumentPersistenceHandler documentPersistenceHandler;
  private final TransformDocumentOutboxHandler transformDocumentOutboxHandler;
  private final TransformDocumentSagaHandler transformDocumentSagaHandler;

  @Transactional
  public TransformDocumentRequestsResult transform(CollectionId collectionId) {
    List<TransformDocumentEvent> transformEvents =
        documentTransformHandler.processTransformRequest(collectionId);

    if (transformEvents.isEmpty()) {
      return TransformDocumentRequestsResult.empty();
    }

    List<Document> documents = transformEvents.stream()
        .map(DocumentEvent::getDocument)
        .toList();
    documentPersistenceHandler.saveAll(documents);

    saveOutboxEntries(transformEvents);

    log.info("Transform request completed - collectionId: {}, total: {}",
        collectionId.getValue(), transformEvents.size());

    return TransformDocumentRequestsResult.from(documents);
  }

  private void saveOutboxEntries(List<TransformDocumentEvent> events) {
    if (!events.isEmpty()) {
      List<SagaStatus> sagaStatuses = events.stream()
          .map(TransformDocumentEvent::getDocument)
          .map(Document::getStatus)
          .map(transformDocumentSagaHandler::documentStatusToSagaStatus)
          .toList();

      transformDocumentOutboxHandler.saveAll(events, sagaStatuses);
      log.info("Batch saved {} outbox entries", events.size());
    }
  }
}
