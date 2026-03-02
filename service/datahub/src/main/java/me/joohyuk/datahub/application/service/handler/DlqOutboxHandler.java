package me.joohyuk.datahub.application.service.handler;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.persistence.DlqOutboxRepository;
import me.joohyuk.datahub.domain.entity.DlqOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DLQ Outbox 핸들러
 * <p>
 * DLQ로 전송할 메시지를 Outbox에 저장하고 관리합니다.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class DlqOutboxHandler {

  private final DlqOutboxRepository dlqOutboxRepository;
  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;

  /**
   * DLQ Outbox를 PENDING 상태로 저장합니다.
   *
   * @param correlationId Saga correlation ID
   * @param originalTopic 원본 토픽 이름
   * @param payload       실패한 메시지의 페이로드
   * @param errorCode     에러 코드
   * @param errorMessage  에러 메시지
   */
  public void save(
      String correlationId,
      String originalTopic,
      String payload,
      String errorCode,
      String errorMessage
  ) {
    DlqOutbox dlqOutbox = DlqOutbox.createPending(
        idGenerator.generateId(),
        correlationId,
        originalTopic,
        payload,
        errorCode,
        errorMessage,
        dateTimeHolder.getCurrentDateTime()
    );

    dlqOutboxRepository.save(dlqOutbox);

    log.info("DLQ Outbox saved. CorrelationId: {}, ErrorCode: {}", correlationId, errorCode);
  }

  /**
   * 완료된(SENT) DLQ Outbox를 삭제합니다.
   * <p>
   * 스케줄러가 주기적으로 호출하여 처리 완료된 메시지를 정리합니다.
   */
  public void deleteSentOutboxMessages() {
    dlqOutboxRepository.deleteByOutboxStatus(OutboxStatus.SENT);
    log.info("Sent DLQ Outbox messages deleted");
  }
}
