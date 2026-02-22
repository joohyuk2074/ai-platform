package me.joohyuk.datarex.infrastructure.adapter.in.message;

import static me.joohyuk.messaging.topics.KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.port.in.TransformDocumentUseCase;
import me.joohyuk.datarex.infrastructure.adapter.in.message.dto.TransformDocumentEventMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentRequestKafkaListener {

  private final TransformDocumentUseCase transformDocumentUseCase;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = DOCUMENT_TRANSFORM_REQUESTED,
      groupId = "datarex-consumer",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onMessage(List<String> jsonMessages) {
    log.info("[DOCUMENT_TRANSFORM_REQUESTED] Received batch size: {}", jsonMessages.size());

    jsonMessages.forEach(json -> {
      try {
        TransformDocumentEventMessage event = objectMapper.readValue(
            json,
            TransformDocumentEventMessage.class
        );

        TransformDocumentCommand command = TransformDocumentCommand.of(event);
        transformDocumentUseCase.transformDocument(command);

      } catch (Exception e) {
        log.error("[DOCUMENT_TRANSFORM_REQUESTED] Failed to deserialize message: {}", json, e);
        // TODO: 필요시 DLQ(Dead Letter Queue)로 전송하거나 별도 모니터링 알림
      }
    });
  }
}
