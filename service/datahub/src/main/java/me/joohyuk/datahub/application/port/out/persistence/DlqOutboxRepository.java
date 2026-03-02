package me.joohyuk.datahub.application.port.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import me.joohyuk.datahub.domain.entity.DlqOutbox;

/**
 * DLQ Outbox 영속성 포트
 */
public interface DlqOutboxRepository {

  /**
   * DLQ Outbox를 저장합니다.
   *
   * @param dlqOutbox DLQ Outbox 도메인 엔티티
   * @return 저장된 DLQ Outbox
   */
  DlqOutbox save(DlqOutbox dlqOutbox);

  /**
   * 특정 상태의 DLQ Outbox를 조회합니다.
   *
   * @param outboxStatus Outbox 상태
   * @return DLQ Outbox 리스트
   */
  List<DlqOutbox> findAllByOutboxStatus(OutboxStatus outboxStatus);

  /**
   * 특정 상태의 DLQ Outbox를 삭제합니다.
   *
   * @param outboxStatus Outbox 상태
   */
  void deleteByOutboxStatus(OutboxStatus outboxStatus);
}
