package me.joohyuk.datahub.infrastructure.adapter.out.message.publisher;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.message.publisher.TransformDocumentMessagePublisher;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentEventKafkaPublisher implements TransformDocumentMessagePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publish(
      TransformDocumentOutbox transformDocumentOutbox,
      BiConsumer<TransformDocumentOutbox, OutboxStatus> outboxCallback
  ) {
    try {
      String key = String.valueOf(transformDocumentOutbox.getSagaId());
      kafkaTemplate.send(
              KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED,
              key,
              transformDocumentOutbox.getPayload()
          )
          .whenComplete((result, ex) -> {
            if (ex == null) {
              log.info("Transform document event sent successfully. SagaId: {}, Offset: {}",
                  transformDocumentOutbox.getSagaId(),
                  result.getRecordMetadata().offset());
              outboxCallback.accept(transformDocumentOutbox, OutboxStatus.SENT);
            } else {
              log.error("Failed to send transform document event. SagaId: {}",
                  transformDocumentOutbox.getSagaId(), ex);
              outboxCallback.accept(transformDocumentOutbox, OutboxStatus.FAILED);
            }
          });
    } catch (Exception e) {
      log.error("Error publishing transform document event. SagaId: {}",
          transformDocumentOutbox.getSagaId(), e);
      outboxCallback.accept(transformDocumentOutbox, OutboxStatus.FAILED);
    }
  }
}
