package me.joohyuk.datahub.application.service.handler;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.port.JsonSerializer;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final IdGenerator idGenerator;
  private final JsonSerializer jsonSerializer;
  private final DateTimeHolder dateTimeHolder;

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
      OutboxStatus outboxStatus
  ) {
    transformDocumentOutboxRepository.deleteByTypeAndOutboxStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        outboxStatus
    );
  }

  @Transactional(readOnly = true)
  public List<TransformDocumentOutbox> getTransformDocumentOutboxByOutboxStatus(
      OutboxStatus outboxStatus
  ) {
    return transformDocumentOutboxRepository.findAllByTypeAndOutboxStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        outboxStatus
    );
  }

  private TransformDocumentOutbox createOutbox(TransformDocumentEvent event) {
    String payload = jsonSerializer.serialize(event);

    return TransformDocumentOutbox.createPending(
        idGenerator.generateId(),
        event.getCorrelationId(),
        payload,
        DocumentStatus.valueOf(event.getDocumentStatus()),
        dateTimeHolder.getCurrentDateTime()
    );
  }
}
