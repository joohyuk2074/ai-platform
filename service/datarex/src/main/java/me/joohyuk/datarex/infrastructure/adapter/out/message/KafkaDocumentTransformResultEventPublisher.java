package me.joohyuk.datarex.infrastructure.adapter.out.message;

import static me.joohyuk.messaging.topics.KafkaTopics.DOCUMENT_TRANSFORM_COMPLETED;
import static me.joohyuk.messaging.topics.KafkaTopics.DOCUMENT_TRANSFORM_FAILED;

import lombok.RequiredArgsConstructor;
import me.joohyuk.datarex.domain.port.out.message.DocumentTransformResultEventPublisher;
import me.joohyuk.messaging.events.DocumentTransformCompletedMessage;
import me.joohyuk.messaging.events.DocumentTransformFailedMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDocumentTransformResultEventPublisher implements
    DocumentTransformResultEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publishCompleted(DocumentTransformCompletedMessage message) {
    kafkaTemplate.send(DOCUMENT_TRANSFORM_COMPLETED, message.documentId(), message);
  }

  @Override
  public void publishFailed(DocumentTransformFailedMessage message) {
    kafkaTemplate.send(DOCUMENT_TRANSFORM_FAILED, message.documentId(), message);
  }
}
