package me.joohyuk.datahub.infrastructure.adapter.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.Collection;
import java.util.List;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.infrastructure.adapter.out.persistence.entity.TransformDocumentOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformDocumentOutboxJpaRepository
    extends JpaRepository<TransformDocumentOutboxJpaEntity, Long> {

  List<TransformDocumentOutboxJpaEntity> findAllByTypeAndOutboxStatusAndSagaStatusIn(
      String type,
      OutboxStatus outboxStatus,
      Collection<SagaStatus> sagaStatuses
  );

  void deleteAllByTypeAndOutboxStatusAndSagaStatusIn(
      String type,
      OutboxStatus outboxStatus,
      List<SagaStatus> sagaStatuses
  );
}
