package me.joohyuk.datarex.infrastructure.adapter.in.message;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.port.in.TransformDocumentRequestMessageListener;
import me.joohyuk.datarex.application.port.in.TransformDocumentUseCase;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentRequestKafkaListener implements
    TransformDocumentRequestMessageListener {

//  private static final String ERROR_CODE_LISTENER_ERROR = "LISTENER_ERROR";

  private final TransformDocumentUseCase transformDocumentUseCase;
//  private final DocumentTransformResultEventPublisher eventPublisher;

  @KafkaListener(
      topics = KafkaTopics.DOCUMENT_TRANSFORM_REQUESTED,
      groupId = "datarex-consumer",
      containerFactory = "kafkaListenerContainerFactory"
  )
  @Override
  public void onMessage(List<TransformDocumentCommand> messages, Acknowledgment ack) {
    log.info("[DOCUMENT_TRANSFORM_REQUESTED] Received batch size: {}", messages.size());
    messages.forEach(transformDocumentUseCase::transformDocument);
  }
//
//  /**
//   * 보상 이벤트가 발행되지 못한 메시지에 대해 Saga 보상 트랜잭션을 수행합니다.
//   *
//   * <p>코레오그래피 Saga 패턴에 따라 {@code document.transform.failed} 토픽으로
//   * {@link DocumentTransformFailedMessage}를 발행합니다. datahub 서비스는 이 이벤트를 수신하여 Document 상태를 업데이트하고
//   * Saga 보상 처리를 완료합니다.
//   *
//   * <p>이 메서드는 {@link DatarexDomainException}이 아닌 예기치 못한 예외로 인해
//   * {@link me.joohyuk.datarex.application.service.TransformDocumentService}의 보상 이벤트 발행 자체가 실패한 경우에만
//   * 호출됩니다.
//   */
//  private void handleFailedMessages(List<TransformDocumentCommand> failedMessages) {
//    log.warn("[SAGA_COMPENSATION] Publishing {} compensation events for unhandled failures",
//        failedMessages.size());
//
//    for (TransformDocumentCommand message : failedMessages) {
//      try {
//        DocumentTransformFailedMessage compensationEvent = new DocumentTransformFailedMessage(
//            UUID.randomUUID().toString(),
//            String.valueOf(message.collectionId()),
//            String.valueOf(message.documentId()),
//            message.contentHash(),
//            ERROR_CODE_LISTENER_ERROR,
//            "Unexpected error occurred during message processing",
//            false,
//            Instant.now()
//        );
//        eventPublisher.publishFailed(compensationEvent);
//
//        log.warn(
//            "[SAGA_COMPENSATION] Published compensation event for trackingId={}, documentId={}",
//            message.trackingId(), message.documentId());
//
//      } catch (Exception e) {
//        log.error(
//            "[SAGA_COMPENSATION] Failed to publish compensation event for trackingId={}, documentId={}: {}",
//            message.trackingId(), message.documentId(), e.getMessage(), e);
//      }
//    }
//  }
}
