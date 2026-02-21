package me.joohyuk.datarex.infrastructure.adapter.out.message;

import static me.joohyuk.messaging.topics.KafkaTopics.DOCUMENT_TRANSFORM_COMPLETED;

import lombok.RequiredArgsConstructor;
import me.joohyuk.datarex.application.port.out.message.DocumentTransformResultEventPublisher;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDocumentTransformResultEventPublisher implements
    DocumentTransformResultEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publishCompleted(TransformDocumentCompletedEvent message) {
    kafkaTemplate.send(DOCUMENT_TRANSFORM_COMPLETED, message.documentId(), message);
  }
}
