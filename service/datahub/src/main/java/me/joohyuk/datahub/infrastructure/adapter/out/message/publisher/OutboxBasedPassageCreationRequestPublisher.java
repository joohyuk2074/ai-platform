package me.joohyuk.datahub.infrastructure.adapter.out.message.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestedEvent;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * {@link PassageCreationRequestPublisher} 포트의 KafkaTemplate 직접 발행 구현체입니다.
 *
 * <p>{@link KafkaTemplate#send}를 통해 {@code passage.creation.requested} 토픽으로 이벤트를
 * 직접 produce합니다. {@code documentId}를 메시지 키로 사용하여 동일 문서의 이벤트 순서를 보장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxBasedPassageCreationRequestPublisher implements PassageCreationRequestPublisher {

  private static final String TOPIC = "passage.creation.requested";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publish(PassageCreationRequestedEvent event) {
    Document document = event.getDocument();

    kafkaTemplate.send(TOPIC, String.valueOf(document.getId().getValue()), event);
    log.info("Passage creation request event published: documentId={}, collectionId={}",
        document.getId().getValue(), document.getCollectionId().getValue());
  }
}
