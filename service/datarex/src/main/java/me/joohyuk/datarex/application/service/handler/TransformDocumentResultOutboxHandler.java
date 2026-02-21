package me.joohyuk.datarex.application.service.handler;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.port.out.peresistence.TransformDocumentResultOutboxRepository;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import me.joohyuk.datarex.domain.exception.DatarexDomainException;
import me.joohyuk.datarex.domain.exception.DatarexErrorCode;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TransformDocumentResultOutboxHandler {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;
  private final TransformDocumentResultOutboxRepository resultOutboxRepository;
  private final ObjectMapper objectMapper;

  public void save(TransformDocumentCompletedEvent event) {
    TransformDocumentResultOutbox outbox = createOutbox(event);
    resultOutboxRepository.save(outbox);
  }


  private TransformDocumentResultOutbox createOutbox(
      TransformDocumentCompletedEvent event
  ) {
    LocalDateTime currentDateTime = dateTimeHolder.getCurrentDateTime();
    String payload = serializeEventToPayload(event);

    return new TransformDocumentResultOutbox(
        idGenerator.generateId(),
        event.sagaId(),
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
