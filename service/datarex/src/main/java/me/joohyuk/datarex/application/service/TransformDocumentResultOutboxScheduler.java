package me.joohyuk.datarex.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.port.out.message.TransformDocumentResultMessagePublisher;
import me.joohyuk.datarex.application.service.handler.TransformDocumentResultOutboxHandler;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentResultOutboxScheduler implements OutboxScheduler {

  private final TransformDocumentResultOutboxHandler transformDocumentResultOutboxHandler;
  private final TransformDocumentResultMessagePublisher transformDocumentResultMessagePublisher;

  @Override
  @Transactional
  @Scheduled(
      fixedDelayString = "${transform-document.outbox-scheduler-fixed-rate:10000}",
      initialDelayString = "${transform-document.outbox-scheduler-initial-delay:10000}"
  )
  public void processOutboxMessage() {
    List<TransformDocumentResultOutbox> outboxMessages = transformDocumentResultOutboxHandler
        .getTransformDocumentResultOutboxStatus(OutboxStatus.PENDING);

    if (outboxMessages.isEmpty()) {
      log.info("No pending TransformDocumentResult outbox messages found to process");
      return;
    }

    log.info("Processing {} TransformDocumentResult outbox messages", outboxMessages.size());

    outboxMessages.forEach(outboxMessage ->
        transformDocumentResultMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));

    log.info("Completed processing TransformDocumentResult outbox messages");
  }

  private void updateOutboxStatus(
      TransformDocumentResultOutbox transformDocumentResultOutbox,
      OutboxStatus outboxStatus
  ) {
    transformDocumentResultOutbox.setOutboxStatus(outboxStatus);
    transformDocumentResultOutboxHandler.save(transformDocumentResultOutbox);
    log.info("TransformDocumentResultOutbox is updated with outbox status: {}",
        outboxStatus.name());
  }
}
