package me.joohyuk.datahub.application.port.out.persistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

public interface TransformDocumentOutboxRepository {

  TransformDocumentOutbox save(TransformDocumentOutbox transformDocumentOutbox);

  List<TransformDocumentOutbox> saveAll(List<TransformDocumentOutbox> transformDocumentOutboxes);

  List<TransformDocumentOutbox> findAllByTypeAndOutboxStatusAndSagaStatus(
      String sagaType,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus
  );
}
