package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.TransformDocumentOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformDocumentOutboxJpaRepository
    extends JpaRepository<TransformDocumentOutboxJpaEntity, Long> {

  List<TransformDocumentOutboxJpaEntity> findAllByTypeAndOutboxStatus(
      String type,
      OutboxStatus outboxStatus
  );

  Optional<TransformDocumentOutboxJpaEntity> findByCorrelationId(String correlationId);

  void deleteAllByTypeAndOutboxStatus(
      String type,
      OutboxStatus outboxStatus
  );
}
