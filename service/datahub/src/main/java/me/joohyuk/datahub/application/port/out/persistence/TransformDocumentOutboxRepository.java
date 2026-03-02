package me.joohyuk.datahub.application.port.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

public interface TransformDocumentOutboxRepository {

  TransformDocumentOutbox save(TransformDocumentOutbox transformDocumentOutbox);

  List<TransformDocumentOutbox> saveAll(List<TransformDocumentOutbox> transformDocumentOutboxes);

  List<TransformDocumentOutbox> findAllByTypeAndOutboxStatus(
      String sagaType,
      OutboxStatus outboxStatus
  );

  Optional<TransformDocumentOutbox> findByCorrelationId(String correlationId);

  void deleteByTypeAndOutboxStatus(
      String documentTransformSagaName,
      OutboxStatus outboxStatus
  );

}
