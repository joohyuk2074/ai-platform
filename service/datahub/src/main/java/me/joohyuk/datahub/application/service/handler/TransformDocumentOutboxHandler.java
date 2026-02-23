package me.joohyuk.datahub.application.service.handler;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.port.JsonSerializer;
import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.port.out.persistence.TransformDocumentOutboxRepository;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransformDocumentOutboxHandler {

  private final TransformDocumentOutboxRepository transformDocumentOutboxRepository;
  private final TransformDocumentSagaHandler transformDocumentSagaHandler;
  private final IdGenerator idGenerator;
  private final JsonSerializer jsonSerializer;

  public void save(TransformDocumentOutbox transformDocumentOutbox) {
    transformDocumentOutboxRepository.save(transformDocumentOutbox);
  }

  public void saveAll(List<TransformDocumentEvent> events) {
    List<TransformDocumentOutbox> outboxes = events.stream()
        .map(this::createOutbox)
        .toList();

    List<TransformDocumentOutbox> savedOutboxes =
        transformDocumentOutboxRepository.saveAll(outboxes);

    log.info("TransformDocumentOutbox bulk saved - count: {}", savedOutboxes.size());
  }

  public void deleteTransformDocumentOutboxMessageByOutboxStatusAndSagaStatus(
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus
  ) {
    transformDocumentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        outboxStatus,
        sagaStatus
    );
  }

  @Transactional(readOnly = true)
  public List<TransformDocumentOutbox> getTransformDocumentOutboxByOutboxStatusAndSagaStatus(
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus
  ) {
    return transformDocumentOutboxRepository.findAllByTypeAndOutboxStatusAndSagaStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        outboxStatus,
        sagaStatus
    );
  }

  private TransformDocumentOutbox createOutbox(TransformDocumentEvent event) {
    String payload = jsonSerializer.serialize(event);

    SagaStatus sagaStatus = transformDocumentSagaHandler.documentStatusNameToSagaStatus(
        event.getStatus()
    );

    return TransformDocumentOutbox.createPending(
        idGenerator.generateId(),
        event.getSagaId(),
        payload,
        DocumentStatus.valueOf(event.getStatus()),
        sagaStatus
    );
  }
}
