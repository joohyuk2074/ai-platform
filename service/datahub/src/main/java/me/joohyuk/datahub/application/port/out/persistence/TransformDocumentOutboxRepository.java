package me.joohyuk.datahub.application.port.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

public interface TransformDocumentOutboxRepository {

  TransformDocumentOutbox save(TransformDocumentOutbox transformDocumentOutbox);

  List<TransformDocumentOutbox> saveAll(List<TransformDocumentOutbox> transformDocumentOutboxes);

  Optional<TransformDocumentOutbox> findBySagaId(Long sagaId);

  List<TransformDocumentOutbox> findAllByTypeAndOutboxStatusAndSagaStatus(
      String sagaType,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus
  );

  void deleteByTypeAndOutboxStatusAndSagaStatus(
      String documentTransformSagaName,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus
  );

}
