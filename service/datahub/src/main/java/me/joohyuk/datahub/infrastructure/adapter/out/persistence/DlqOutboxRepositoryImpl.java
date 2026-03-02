package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.application.port.out.persistence.DlqOutboxRepository;
import me.joohyuk.datahub.domain.entity.DlqOutbox;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.DlqOutboxJpaEntity;
import org.springframework.stereotype.Repository;

/**
 * DLQ Outbox Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class DlqOutboxRepositoryImpl implements DlqOutboxRepository {

  private final DlqOutboxJpaRepository jpaRepository;

  @Override
  public DlqOutbox save(DlqOutbox dlqOutbox) {
    DlqOutboxJpaEntity jpaEntity = DlqOutboxJpaEntity.from(dlqOutbox);
    DlqOutboxJpaEntity savedJpaEntity = jpaRepository.save(jpaEntity);
    return savedJpaEntity.toDomain();
  }

  @Override
  public List<DlqOutbox> findAllByOutboxStatus(OutboxStatus outboxStatus) {
    return jpaRepository.findAllByOutboxStatus(outboxStatus)
        .stream()
        .map(DlqOutboxJpaEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
    jpaRepository.deleteAllByOutboxStatus(outboxStatus);
  }
}
