package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.util.DateTimeHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.in.service.HandleTransformCompletedUseCase;
import me.joohyuk.datahub.application.port.out.persistence.TransformDocumentOutboxRepository;
import me.joohyuk.datahub.application.service.handler.DlqOutboxHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.vo.TransformErrorClassifier;
import me.joohyuk.datahub.infrastructure.adapter.in.listener.dto.TransformDocumentCompletedEvent;
import me.joohyuk.messaging.topics.KafkaTopics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleTransformCompletedService implements HandleTransformCompletedUseCase {

  private final TransformDocumentOutboxRepository transformDocumentOutboxRepository;
  private final TransformDocumentHandler transformDocumentHandler;
  private final DlqOutboxHandler dlqOutboxHandler;
  private final DateTimeHolder dateTimeHolder;

  @Override
  @Transactional
  public void handleCompleted(TransformDocumentCompletedEvent event) {
    log.info("Processing transform completed event. CorrelationId: {}, DocumentId: {}",
        event.correlationId(), event.documentId());

    // 1. correlationId로 Outbox 조회
    TransformDocumentOutbox outbox = transformDocumentOutboxRepository
        .findByCorrelationId(event.correlationId())
        .orElse(null);

    if (outbox == null) {
      log.warn("Outbox not found for correlationId: {}. Skipping processing.",
          event.correlationId());
      return;
    }

    try {
      DocumentId documentId = DocumentId.from(event.documentId());

      if (event.transformCompleted()) {
        handleSuccess(event, outbox, documentId);
      } else {
        handleFailure(event, outbox, documentId);
      }

    } catch (Exception e) {
      log.error("Error processing transform completed event. CorrelationId: {}, ",
          event.correlationId(), e);
      // 예외 발생 시 Outbox를 FAILED로 마킹하여 재처리 방지
      outbox.markFailed(dateTimeHolder.getCurrentDateTime());
      transformDocumentOutboxRepository.save(outbox);
      throw new DatahubDomainException("", DatahubErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private void handleSuccess(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      DocumentId documentId
  ) {
    log.info("Transform succeeded. DocumentId: {}, PassageCount: {}",
        event.documentId(), event.passageCount());

    // Document 상태 업데이트: TRANSFORM_REQUESTED -> TRANSFORMED
    transformDocumentHandler.complete(documentId, event.passageCount(), event.correlationId());

    // Outbox 완료 처리
    outbox.markCompleted(dateTimeHolder.getCurrentDateTime());
    transformDocumentOutboxRepository.save(outbox);

    log.info("Transform completed successfully processed. DocumentId: {}, SagaId: {}",
        event.documentId(), event.correlationId());
  }

  private void handleFailure(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      DocumentId documentId
  ) {
    String errorCode = event.errorCode();
    String errorMessage = event.errorMessage();

    log.warn("Transform failed. DocumentId: {}, ErrorCode: {}, ErrorMessage: {}",
        event.documentId(), errorCode, errorMessage);

    // 에러 분류
    if (TransformErrorClassifier.isRetryable(errorCode)) {
      handleRetryableError(event, outbox, documentId, errorCode, errorMessage);
    } else if (TransformErrorClassifier.isPermanent(errorCode)) {
      handlePermanentError(event, outbox, documentId, errorCode, errorMessage);
    } else {
      // 알 수 없는 오류는 안전하게 DLQ로 전송
      handleUnknownError(event, outbox, documentId, errorCode, errorMessage);
    }
  }

  private void handleRetryableError(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      DocumentId documentId,
      String errorCode,
      String errorMessage
  ) {
    log.info("Retryable error detected. DocumentId: {}, ErrorCode: {}. " +
            "Reverting outbox to PENDING for retry.",
        event.documentId(), errorCode);

    // Document를 실패 상태로 업데이트
    transformDocumentHandler.failed(documentId, errorCode, errorMessage, event.correlationId());

    // Outbox를 PENDING으로 되돌려서 스케줄러가 재처리하도록 함
    outbox.markForRetry();
    transformDocumentOutboxRepository.save(outbox);

    log.info("Document marked as failed and outbox reverted to PENDING for retry. DocumentId: {}",
        event.documentId());
  }

  private void handlePermanentError(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      DocumentId documentId,
      String errorCode,
      String errorMessage
  ) {
    log.error("Permanent error detected. Sending to DLQ. DocumentId: {}, ErrorCode: {}",
        event.documentId(), errorCode);

    // Document 실패 처리
    transformDocumentHandler.failed(documentId, errorCode, errorMessage, event.correlationId());

    // DLQ 전송
    sendToDLQ(event, outbox, errorCode, errorMessage);

    // Outbox 실패 처리
    outbox.markFailed(dateTimeHolder.getCurrentDateTime());
    transformDocumentOutboxRepository.save(outbox);
  }

  private void handleUnknownError(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      DocumentId documentId,
      String errorCode,
      String errorMessage
  ) {
    log.error("Unknown error detected. Sending to DLQ for safety. DocumentId: {}, ErrorCode: {}",
        event.documentId(), errorCode);

    // Document 실패 처리
    transformDocumentHandler.failed(documentId, errorCode, errorMessage, event.correlationId());

    // DLQ 전송
    sendToDLQ(event, outbox, errorCode, errorMessage);

    // Outbox 실패 처리
    outbox.markFailed(dateTimeHolder.getCurrentDateTime());
    transformDocumentOutboxRepository.save(outbox);
  }

  private void sendToDLQ(
      TransformDocumentCompletedEvent event,
      TransformDocumentOutbox outbox,
      String errorCode,
      String errorMessage
  ) {
    // DLQ Outbox에 저장 (스케줄러가 폴링하여 Kafka로 전송)
    dlqOutboxHandler.save(
        String.valueOf(event.correlationId()),
        KafkaTopics.DOCUMENT_TRANSFORM_RESULT,
        outbox.getPayload(),
        errorCode,
        errorMessage
    );
  }
}
