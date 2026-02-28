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
      String key = transformDocumentOutbox.getCorrelationId();
      kafkaTemplate.send(
              KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED,
              key,
              transformDocumentOutbox.getPayload()
          )
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Failed to send transform document event. CorrelationId: {}",
                  transformDocumentOutbox.getCorrelationId(), ex);
              outboxCallback.accept(transformDocumentOutbox, OutboxStatus.FAILED);
              return;
            }

            log.info("Transform document event sent successfully. CorrelationId: {}, Offset: {}",
                transformDocumentOutbox.getCorrelationId(),
                result.getRecordMetadata().offset());
            outboxCallback.accept(transformDocumentOutbox, OutboxStatus.SENT);
          });
    } catch (Exception e) {
      log.error("Error publishing transform document event. CorrelationId: {}",
          transformDocumentOutbox.getCorrelationId(), e);
      outboxCallback.accept(transformDocumentOutbox, OutboxStatus.FAILED);
    }
  }
}
