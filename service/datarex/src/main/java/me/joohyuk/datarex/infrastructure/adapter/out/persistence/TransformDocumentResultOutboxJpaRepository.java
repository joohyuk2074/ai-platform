package me.joohyuk.datarex.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datarex.infrastructure.adapter.out.persistence.entity.TransformDocumentResultOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformDocumentResultOutboxJpaRepository
    extends JpaRepository<TransformDocumentResultOutboxJpaEntity, Long> {

  List<TransformDocumentResultOutboxJpaEntity> findAllByTypeAndOutboxStatus(
      String type,
      OutboxStatus outboxStatus
  );

  Optional<TransformDocumentResultOutboxJpaEntity> findBySagaId(Long sagaId);
}
