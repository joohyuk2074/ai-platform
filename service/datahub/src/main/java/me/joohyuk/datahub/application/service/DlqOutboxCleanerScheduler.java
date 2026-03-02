package me.joohyuk.datahub.application.service;

import com.spartaecommerce.outbox.OutboxScheduler;
import com.spartaecommerce.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.service.handler.DlqOutboxHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * DLQ Outbox 정리 스케줄러
 * <p>
 * 전송 완료된(SENT) DLQ Outbox를 주기적으로 삭제하여 테이블 크기를 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqOutboxCleanerScheduler implements OutboxScheduler {

  private final DlqOutboxHandler dlqOutboxHandler;

  @Override
  @Scheduled(cron = "${dlq.outbox-cleaner-cron:0 0 2 * * ?}")  // 기본값: 매일 새벽 2시
  public void processOutboxMessage() {
    log.info("Starting DLQ Outbox cleanup");

    dlqOutboxHandler.deleteSentOutboxMessages();

    log.info("Completed DLQ Outbox cleanup");
  }
}
