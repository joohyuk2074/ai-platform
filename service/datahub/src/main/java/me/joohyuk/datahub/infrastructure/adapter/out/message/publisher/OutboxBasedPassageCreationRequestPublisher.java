package me.joohyuk.datahub.infrastructure.adapter.out.message.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.infrastructure.adapter.PassageMessagingDataMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxBasedPassageCreationRequestPublisher implements PassageCreationRequestPublisher {

  // TODO: 환경변수로 관리
  private static final String TOPIC = "passage.creation.requested";

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final PassageMessagingDataMapper passageMessagingDataMapper;

  @Override
  public void publish(PassageCreationRequestEvent event) {
    // TODO: avro 직렬화 적용
    Document document = event.getDocument();

    kafkaTemplate.send(TOPIC, String.valueOf(document.getId().getValue()), event);
    log.info("Passage creation request event published: documentId={}, collectionId={}",
        document.getId().getValue(), document.getCollectionId().getValue());
  }
}
