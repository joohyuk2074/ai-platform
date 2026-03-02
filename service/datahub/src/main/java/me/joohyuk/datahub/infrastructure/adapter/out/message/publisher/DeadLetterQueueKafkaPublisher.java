package me.joohyuk.datahub.infrastructure.adapter.out.message.publisher;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.message.publisher.DeadLetterQueuePublisher;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueKafkaPublisher implements DeadLetterQueuePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publish(
      String topic,
      String key,
      String payload,
      String errorCode,
      String errorMessage,
      String correlationId
  ) {
    try {
      Map<String, Object> dlqMessage = new HashMap<>();
      dlqMessage.put("originalTopic", topic);
      dlqMessage.put("correlationId", correlationId);
      dlqMessage.put("key", key);
      dlqMessage.put("payload", payload);
      dlqMessage.put("errorCode", errorCode);
      dlqMessage.put("errorMessage", errorMessage);
      dlqMessage.put("timestamp", System.currentTimeMillis());

      kafkaTemplate.send(KafkaTopics.DOCUMENT_TRANSFORM_DLQ, correlationId, dlqMessage)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              log.error("Failed to send message to DLQ. CorrelationId: {}, ErrorCode: {}",
                  correlationId, errorCode, ex);
              return;
            }

            log.warn("Message sent to DLQ. CorrelationId: {}, ErrorCode: {}, Offset: {}",
                correlationId,
                errorCode,
                result.getRecordMetadata().offset());
          });
    } catch (Exception e) {
      log.error("Error publishing to DLQ. CorrelationId: {}, ErrorCode: {}",
          correlationId, errorCode, e);
    }
  }
}
