package me.joohyuk.datahub.infrastructure.adapter.out.message.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import me.joohyuk.datahub.application.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.infrastructure.adapter.PassageMessagingDataMapper;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxBasedPassageCreationRequestPublisher implements PassageCreationRequestPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final PassageMessagingDataMapper passageMessagingDataMapper;

  @Override
  public void publish(TransformDocumentEvent event) {

    // TODO: avro 직렬화 적용
    DocumentTransformRequestedMessage message =
        passageMessagingDataMapper.eventToMessage(event);

    kafkaTemplate.send(
        KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED,
        String.valueOf(message.message().documentId()),
        message
    );

    log.info("Document transform requested message published: documentId={}, collectionId={}",
        message.message().documentId(), message.message().collectionId());
  }
}
