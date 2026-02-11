package me.joohyuk.datarex.infrastructure.adapter.in.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.port.in.DocumentTransformRequestMessageListener;
import me.joohyuk.datarex.application.port.in.DocumentTransformUseCase;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTransformRequestKafkaListener implements
    DocumentTransformRequestMessageListener {

  private final DocumentTransformUseCase documentTransformUseCase;

  @KafkaListener(
      topics = KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED,
      groupId = "datarex-consumer",
      containerFactory = "kafkaListenerContainerFactory"
  )
  @Override
  public void onMessage(DocumentTransformRequestedMessage message) {
    documentTransformUseCase.transformDocument(message);
  }
}
