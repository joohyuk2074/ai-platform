package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.DlqOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DLQ Outbox JPA Repository
 */
public interface DlqOutboxJpaRepository extends JpaRepository<DlqOutboxJpaEntity, Long> {

  /**
   * 특정 상태의 DLQ Outbox를 조회합니다.
   *
   * @param outboxStatus Outbox 상태
   * @return DLQ Outbox JPA 엔티티 리스트
   */
  List<DlqOutboxJpaEntity> findAllByOutboxStatus(OutboxStatus outboxStatus);

  /**
   * 특정 상태의 DLQ Outbox를 삭제합니다.
   *
   * @param outboxStatus Outbox 상태
   */
  void deleteAllByOutboxStatus(OutboxStatus outboxStatus);
}
