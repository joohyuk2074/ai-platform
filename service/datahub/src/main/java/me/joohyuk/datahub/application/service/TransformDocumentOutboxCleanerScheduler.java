package me.joohyuk.datahub.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentOutboxCleanerScheduler implements OutboxScheduler {

  private final TransformDocumentOutboxHandler transformDocumentOutboxHandler;

  @Override
  @Scheduled(cron = "@midnight")
  public void processOutboxMessage() {
    List<TransformDocumentOutbox> outboxMessages =
        transformDocumentOutboxHandler.getTransformDocumentOutboxByOutboxStatus(
            OutboxStatus.SENT
        );

    transformDocumentOutboxHandler.deleteTransformDocumentOutboxMessageByOutboxStatusAndSagaStatus(
        OutboxStatus.SENT
    );

    log.info("Received {} TransformDocumentOutbox messages for clean-up.", outboxMessages.size());
  }
}
