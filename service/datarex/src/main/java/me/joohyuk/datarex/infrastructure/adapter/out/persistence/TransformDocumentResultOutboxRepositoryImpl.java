package me.joohyuk.datarex.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datarex.application.port.out.peresistence.TransformDocumentResultOutboxRepository;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import me.joohyuk.datarex.infrastructure.adapter.out.persistence.entity.TransformDocumentResultOutboxJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransformDocumentResultOutboxRepositoryImpl implements
    TransformDocumentResultOutboxRepository {

  private final TransformDocumentResultOutboxJpaRepository jpaRepository;

  @Override
  public TransformDocumentResultOutbox save(TransformDocumentResultOutbox outbox) {
    TransformDocumentResultOutboxJpaEntity entity =
        TransformDocumentResultOutboxJpaEntity.from(outbox);
    TransformDocumentResultOutboxJpaEntity saved = jpaRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public List<TransformDocumentResultOutbox> findAllByOutboxStatus(
      String sagaType,
      OutboxStatus outboxStatus
  ) {
    return jpaRepository.findAllByTypeAndOutboxStatus(sagaType, outboxStatus).stream()
        .map(TransformDocumentResultOutboxJpaEntity::toDomain)
        .toList();
  }

  @Override
  public Optional<TransformDocumentResultOutbox> findByCorrelationId(String correlationId) {
    return jpaRepository.findByCorrelationId(correlationId)
        .map(TransformDocumentResultOutboxJpaEntity::toDomain);
  }
}
