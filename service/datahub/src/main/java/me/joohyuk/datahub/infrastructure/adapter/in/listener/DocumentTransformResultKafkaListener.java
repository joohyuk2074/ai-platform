package me.joohyuk.datahub.infrastructure.adapter.in.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * datarex 서비스에서 발행하는 Document Transform 결과 이벤트를 수신하는 Kafka 리스너.
 *
 * <p>두 개의 토픽을 구독합니다:
 * <ul>
 *   <li>document.transform.completed: Transform 성공</li>
 *   <li>document.transform.failed: Transform 실패</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTransformResultKafkaListener {
//
//  private final TransformDocumentListener transformDocumentListener;
//
//  /**
//   * Document Transform 완료 메시지 수신.
//   *
//   * @param message datarex에서 발행한 완료 메시지
//   */
//  @KafkaListener(
//      topics = "document.transform.completed",
//      groupId = "datahub-consumer",
//      containerFactory = "kafkaListenerContainerFactory"
//  )
//  public void onTransformCompleted(
//      DocumentTransformCompletedMessage message,
//      Acknowledgment acknowledgment
//  ) {
//    log.info("Received transform completed message: eventId={}, documentId={}, passageCount={}",
//        message.eventId(), message.documentId(), message.passageCount());
//
//    try {
//      PassageResponse response = PassageResponse.builder()
//          .eventId(message.eventId())
//          .collectionId(Long.parseLong(message.collectionId()))
//          .documentId(Long.parseLong(message.documentId()))
//          .contentHash(message.contentHash())
//          .passageCount(message.passageCount())
//          .success(true)
//          .occurredAt(message.occurredAt())
//          .build();
//
//      transformDocumentListener.onCompleted(response);
//
//      // 수동 커밋
//      if (acknowledgment != null) {
//        acknowledgment.acknowledge();
//      }
//
//      log.info("Transform completed processed successfully: documentId={}", message.documentId());
//    } catch (Exception e) {
//      log.error("Failed to process transform completed message: documentId={}", message.documentId(), e);
//      // TODO: Dead Letter Queue로 전송하거나 재시도 로직 추가
//      // 커밋하지 않으면 재처리됨
//    }
//  }
//
//  /**
//   * Document Transform 실패 메시지 수신.
//   *
//   * @param message datarex에서 발행한 실패 메시지
//   */
//  @KafkaListener(
//      topics = "document.transform.failed",
//      groupId = "datahub-consumer",
//      containerFactory = "kafkaListenerContainerFactory"
//  )
//  public void onTransformFailed(DocumentTransformFailedMessage message, Acknowledgment acknowledgment) {
//    log.warn("Received transform failed message: eventId={}, documentId={}, errorCode={}",
//        message.eventId(), message.documentId(), message.errorCode());
//
//    try {
//      PassageResponse response = PassageResponse.builder()
//          .eventId(message.eventId())
//          .collectionId(Long.parseLong(message.collectionId()))
//          .documentId(Long.parseLong(message.documentId()))
//          .contentHash(message.contentHash())
//          .errorCode(message.errorCode())
//          .errorMessage(message.errorMessage())
//          .success(false)
//          .occurredAt(message.occurredAt())
//          .build();
//
//      transformDocumentListener.onFailed(response);
//
//      // 수동 커밋
//      if (acknowledgment != null) {
//        acknowledgment.acknowledge();
//      }
//
//      log.info("Transform failed processed successfully: documentId={}, errorCode={}",
//          message.documentId(), message.errorCode());
//    } catch (Exception e) {
//      log.error("Failed to process transform failed message: documentId={}", message.documentId(), e);
//      // TODO: Dead Letter Queue로 전송하거나 재시도 로직 추가
//      // 커밋하지 않으면 재처리됨
//    }
//  }
}
