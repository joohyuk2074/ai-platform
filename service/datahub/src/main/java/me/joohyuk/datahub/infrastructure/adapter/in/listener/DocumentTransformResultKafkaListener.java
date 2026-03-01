package me.joohyuk.datahub.infrastructure.adapter.in.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.in.service.HandleTransformCompletedUseCase;
import me.joohyuk.datahub.infrastructure.adapter.in.listener.dto.TransformDocumentCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTransformResultKafkaListener {

  private final HandleTransformCompletedUseCase handleTransformCompletedUseCase;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "document.transform.result",
      groupId = "datahub-consumer",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onTransformCompleted(
      List<String> jsonMessages
  ) {
    jsonMessages.forEach(json -> {
      try {
        TransformDocumentCompletedEvent event = objectMapper.readValue(
            json,
            TransformDocumentCompletedEvent.class
        );

        handleTransformCompletedUseCase.handleCompleted(event);

        log.info("Successfully processed transform completed event: documentId={}",
            event.documentId());

      } catch (Exception e) {
        log.error("Failed to process transform completed message: json={}", json, e);
        // 예외가 발생하면 Kafka 컨슈머가 재시도하거나 에러 핸들러로 처리됨
        throw new RuntimeException("Failed to process transform completed event", e);
      }
    });
  }
}
