package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;
import me.joohyuk.datahub.application.port.in.service.TransformDocumentUseCase;
import me.joohyuk.datahub.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentSagaHandler;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentService implements TransformDocumentUseCase {

  private final TransformDocumentHandler transformDocumentHandler;
  private final TransformDocumentOutboxHandler transformDocumentOutboxHandler;
  private final TransformDocumentSagaHandler transformDocumentSagaHandler;

  @Transactional
  public TransformDocumentRequestsResult transform(CollectionId collectionId) {
    List<TransformDocumentEvent> events =
        transformDocumentHandler.processTransformRequest(collectionId);

    if (events.isEmpty()) {
      return TransformDocumentRequestsResult.empty();
    }

    saveOutboxEntries(events);

    log.info("Transform request completed - collectionId: {}, total: {}",
        collectionId.getValue(), events.size());

    return TransformDocumentRequestsResult.fromEvents(events);
  }

  private void saveOutboxEntries(List<TransformDocumentEvent> events) {
    List<SagaStatus> sagaStatuses = events.stream()
        .map(TransformDocumentEvent::getStatus)
        .map(transformDocumentSagaHandler::documentStatusNameToSagaStatus)
        .toList();

    transformDocumentOutboxHandler.saveAll(events, sagaStatuses);
    log.info("Batch saved {} outbox entries", events.size());
  }
}
