package me.joohyuk.datarex.infrastructure.adapter.out.message;

import static me.joohyuk.messaging.topics.KafkaTopics.DOCUMENT_TRANSFORM_RESULT;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.port.out.message.TransformDocumentResultMessagePublisher;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDocumentTransformResultEventPublisher implements
    TransformDocumentResultMessagePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publish(
      TransformDocumentResultOutbox resultOutbox,
      BiConsumer<TransformDocumentResultOutbox, OutboxStatus> outboxCallback
  ) {

    try {
      String key = String.valueOf(resultOutbox.getCorrelationId());
      kafkaTemplate.send(
              DOCUMENT_TRANSFORM_RESULT,
              key,
              resultOutbox.getPayload()
          )
          .whenComplete((result, ex) -> {
            if (ex == null) {
              log.info(
                  "Document transform result outbox published successfully. SagaId: {}, Topic: {}, Offset: {}",
                  resultOutbox.getCorrelationId(),
                  DOCUMENT_TRANSFORM_RESULT,
                  result.getRecordMetadata().offset());
              outboxCallback.accept(resultOutbox, OutboxStatus.SENT);
            } else {
              log.error("Failed to publish document transform result outbox. SagaId: {}",
                  resultOutbox.getCorrelationId(), ex);
              outboxCallback.accept(resultOutbox, OutboxStatus.FAILED);
            }
          });
    } catch (Exception e) {
      log.error("Error publishing document transform result outbox. SagaId: {}",
          resultOutbox.getCorrelationId(), e);
      outboxCallback.accept(resultOutbox, OutboxStatus.FAILED);
    }

  }
}
