package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.joohyuk.datahub.application.port.out.persistence.TransformDocumentOutboxRepository;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.TransformDocumentOutboxJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransformDocumentOutboxRepositoryImpl implements TransformDocumentOutboxRepository {

  private final TransformDocumentOutboxJpaRepository jpaRepository;

  @Override
  public TransformDocumentOutbox save(TransformDocumentOutbox outboxMessage) {
    TransformDocumentOutboxJpaEntity jpaEntity =
        TransformDocumentOutboxJpaEntity.from(outboxMessage);
    TransformDocumentOutboxJpaEntity savedJpaEntity = jpaRepository.save(jpaEntity);
    return savedJpaEntity.toDomain();
  }

  @Override
  public List<TransformDocumentOutbox> saveAll(
      List<TransformDocumentOutbox> transformDocumentOutboxes) {
    List<TransformDocumentOutboxJpaEntity> jpaEntities = transformDocumentOutboxes.stream()
        .map(TransformDocumentOutboxJpaEntity::from)
        .toList();

    List<TransformDocumentOutboxJpaEntity> savedJpaEntities = jpaRepository.saveAll(jpaEntities);

    return savedJpaEntities.stream()
        .map(TransformDocumentOutboxJpaEntity::toDomain)
        .toList();
  }

  @Override
  public void deleteByTypeAndOutboxStatus(
      String documentTransformSagaName,
      OutboxStatus outboxStatus
  ) {
    jpaRepository.deleteAllByTypeAndOutboxStatus(
        documentTransformSagaName,
        outboxStatus
    );
  }

  @Override
  public List<TransformDocumentOutbox> findAllByTypeAndOutboxStatus(
      String type,
      OutboxStatus outboxStatus
  ) {
    return jpaRepository.findAllByTypeAndOutboxStatus(
            type,
            outboxStatus
        )
        .stream()
        .map(TransformDocumentOutboxJpaEntity::toDomain)
        .collect(Collectors.toList());
  }
}
