package me.joohyuk.datahub.application.service.handler;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.port.out.persistence.TransformDocumentOutboxRepository;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransformDocumentOutboxHandler {

  private final TransformDocumentOutboxRepository transformDocumentOutboxRepository;
  private final IdGenerator idGenerator;
  private final ObjectMapper objectMapper;

  public void save(TransformDocumentOutbox transformDocumentOutbox) {
    transformDocumentOutboxRepository.save(transformDocumentOutbox);
  }

  public void saveAll(
      List<TransformDocumentEvent> events,
      List<SagaStatus> sagaStatuses
  ) {
    if (events.size() != sagaStatuses.size()) {
      throw new DatahubDomainException(
          "Events and sagaStatuses must have the same size",
          DatahubErrorCode.INVALID_REQUEST
      );
    }

    List<TransformDocumentOutbox> outboxes = new ArrayList<>();
    for (int i = 0; i < events.size(); i++) {
      Long sagaId = idGenerator.generateId();
      outboxes.add(createOutbox(events.get(i), sagaId, sagaStatuses.get(i)));
    }

    List<TransformDocumentOutbox> savedOutboxes = transformDocumentOutboxRepository.saveAll(
        outboxes);

    log.info("TransformDocumentOutbox bulk saved - count: {}", savedOutboxes.size());
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

  private TransformDocumentOutbox createOutbox(
      TransformDocumentEvent event,
      Long sagaId,
      SagaStatus sagaStatus
  ) {
    String payload = serializeEventToPayload(event);

    return TransformDocumentOutbox.builder()
        .id(idGenerator.generateId())
        .sagaId(sagaId)
        .type("TransformDocumentRequested")
        .payload(payload)
        .documentStatus(event.getDocument().getStatus())
        .sagaStatus(sagaStatus)
        .outboxStatus(OutboxStatus.PENDING)
        .version(0)
        .createdAt(LocalDateTime.now())
        .processedAt(null)
        .build();
  }

  private String serializeEventToPayload(TransformDocumentEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize TransformDocumentEvent to JSON", e);
      throw new DatahubDomainException(
          "Failed to serialize event to JSON payload",
          DatahubErrorCode.INTERNAL_SERVER_ERROR,
          e
      );
    }
  }
}
