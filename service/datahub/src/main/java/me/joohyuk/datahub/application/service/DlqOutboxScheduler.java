package me.joohyuk.datahub.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.message.publisher.DeadLetterQueuePublisher;
import me.joohyuk.datahub.application.port.out.persistence.DlqOutboxRepository;
import me.joohyuk.datahub.domain.entity.DlqOutbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DLQ Outbox 스케줄러
 * <p>
 * PENDING 상태의 DLQ Outbox를 주기적으로 폴링하여 Kafka DLQ 토픽으로 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqOutboxScheduler implements OutboxScheduler {

  private final DlqOutboxRepository dlqOutboxRepository;
  private final DeadLetterQueuePublisher deadLetterQueuePublisher;
  private final DateTimeHolder dateTimeHolder;

  @Override
  @Transactional
  @Scheduled(
      fixedDelayString = "${dlq.outbox-scheduler-fixed-rate:10000}",
      initialDelayString = "${dlq.outbox-scheduler-initial-delay:10000}"
  )
  public void processOutboxMessage() {
    List<DlqOutbox> outboxMessages =
        dlqOutboxRepository.findAllByOutboxStatus(OutboxStatus.PENDING);

    if (outboxMessages.isEmpty()) {
      log.debug("No pending DLQ outbox messages found to process");
      return;
    }

    log.info("Processing {} DLQ outbox messages", outboxMessages.size());

    outboxMessages.forEach(this::publishToDlq);

    log.info("Completed processing DLQ outbox messages");
  }

  private void publishToDlq(DlqOutbox dlqOutbox) {
    try {
      // DLQ로 메시지 전송
      deadLetterQueuePublisher.publish(
          dlqOutbox.getOriginalTopic(),
          dlqOutbox.getCorrelationId(),
          dlqOutbox.getPayload(),
          dlqOutbox.getErrorCode(),
          dlqOutbox.getErrorMessage(),
          dlqOutbox.getCorrelationId()
      );

      // 전송 성공 시 SENT로 변경
      dlqOutbox.markSent(dateTimeHolder.getCurrentDateTime());
      dlqOutboxRepository.save(dlqOutbox);

      log.info("DLQ message sent successfully. CorrelationId: {}, ErrorCode: {}",
          dlqOutbox.getCorrelationId(), dlqOutbox.getErrorCode());

    } catch (Exception e) {
      // 전송 실패 시 FAILED로 변경
      log.error("Failed to send DLQ message. CorrelationId: {}, ErrorCode: {}",
          dlqOutbox.getCorrelationId(), dlqOutbox.getErrorCode(), e);

      dlqOutbox.markFailed(dateTimeHolder.getCurrentDateTime());
      dlqOutboxRepository.save(dlqOutbox);
    }
  }
}
