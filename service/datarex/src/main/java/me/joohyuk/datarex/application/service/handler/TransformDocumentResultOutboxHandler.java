package me.joohyuk.datarex.application.service.handler;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.port.out.peresistence.TransformDocumentResultOutboxRepository;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.exception.DatarexErrorCode;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransformDocumentResultOutboxHandler {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;
  private final TransformDocumentResultOutboxRepository resultOutboxRepository;
  private final ObjectMapper objectMapper;

  public void save(TransformDocumentResultOutbox transformDocumentResultOutbox) {
    resultOutboxRepository.save(transformDocumentResultOutbox);
  }

  public void save(TransformDocumentCompletedEvent event) {
    if (resultOutboxRepository.findByCorrelationId(event.correlationId()).isPresent()) {
      log.warn(
          "Outbox already exists for correlationId: {}, skipping duplicate processing",
          event.collectionId()
      );
      return;
    }

    TransformDocumentResultOutbox outbox = createOutbox(event);
    resultOutboxRepository.save(outbox);
  }

  @Transactional(readOnly = true)
  public List<TransformDocumentResultOutbox> getTransformDocumentResultOutboxStatus(
      OutboxStatus outboxStatus
  ) {
    return resultOutboxRepository.findAllByOutboxStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        outboxStatus
    );
  }

  private TransformDocumentResultOutbox createOutbox(
      TransformDocumentCompletedEvent event
  ) {
    LocalDateTime currentDateTime = dateTimeHolder.getCurrentDateTime();
    String payload = serializeEventToPayload(event);

    return new TransformDocumentResultOutbox(
        idGenerator.generateId(),
        event.correlationId(),
        DOCUMENT_TRANSFORM_SAGA_NAME,
        OutboxStatus.PENDING,
        payload,
        currentDateTime,
        currentDateTime
    );
  }

  private String serializeEventToPayload(TransformDocumentCompletedEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize TransformDocumentEvent to JSON", e);
      throw new DatarexDomainException(
          "Failed to serialize event to JSON payload",
          DatarexErrorCode.SERIALIZATION_FAILED,
          e
      );
    }
  }

}
