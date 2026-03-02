package me.joohyuk.datahub.application.service;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.persistence.TransformDocumentOutboxRepository;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformDocumentOutboxCleanerScheduler implements OutboxScheduler {

  private final TransformDocumentOutboxRepository transformDocumentOutboxRepository;

  @Override
  @Scheduled(cron = "@midnight")
  public void processOutboxMessage() {
    List<TransformDocumentOutbox> outboxMessages =
        transformDocumentOutboxRepository.findAllByTypeAndOutboxStatus(
            DOCUMENT_TRANSFORM_SAGA_NAME,
            OutboxStatus.SENT
        );

    transformDocumentOutboxRepository.deleteByTypeAndOutboxStatus(
        DOCUMENT_TRANSFORM_SAGA_NAME,
        OutboxStatus.SENT
    );

    log.info("Received {} TransformDocumentOutbox messages for clean-up.", outboxMessages.size());
  }
}
