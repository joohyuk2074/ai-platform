package me.joohyuk.datahub.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.message.publisher.TransformDocumentMessagePublisher;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
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
      fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
      initialDelayString = "${order-service.outbox-scheduler-initial-delay}"
  )
  public void processOutboxMessage() {

  }
}
