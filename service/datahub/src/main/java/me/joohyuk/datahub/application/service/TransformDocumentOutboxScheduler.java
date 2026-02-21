package me.joohyuk.datahub.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.port.out.message.publisher.TransformDocumentMessagePublisher;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentOutboxScheduler implements OutboxScheduler {

  private final TransformDocumentOutboxHandler transformDocumentOutboxHandler;
  private final TransformDocumentMessagePublisher transformDocumentMessagePublisher;

  @Override
  @Transactional
  @Scheduled(
      fixedDelayString = "${transform-document.outbox-scheduler-fixed-rate:10000}",
      initialDelayString = "${transform-document.outbox-scheduler-initial-delay:10000}"
  )
  public void processOutboxMessage() {
    List<TransformDocumentOutbox> outboxMessages =
        transformDocumentOutboxHandler.getTransformDocumentOutboxByOutboxStatusAndSagaStatus(
            OutboxStatus.PENDING,
            SagaStatus.STARTED,
            SagaStatus.COMPENSATING
        );

    if (outboxMessages.isEmpty()) {
      log.info("No pending TransformDocument outbox messages found to process");
      return;
    }

    log.info("Processing {} TransformDocument outbox messages", outboxMessages.size());

    outboxMessages.forEach(outboxMessage ->
        transformDocumentMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));

    log.info("Completed processing TransformDocument outbox messages");
  }

  private void updateOutboxStatus(
      TransformDocumentOutbox transformDocumentOutbox,
      OutboxStatus outboxStatus
  ) {
    transformDocumentOutbox.setOutboxStatus(outboxStatus);
    transformDocumentOutboxHandler.save(transformDocumentOutbox);
    log.info("TransformDocumentOutbox is updated with outbox status: {}", outboxStatus.name());
  }
}
