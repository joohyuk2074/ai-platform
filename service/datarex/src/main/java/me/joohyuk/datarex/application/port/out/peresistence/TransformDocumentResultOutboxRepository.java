package me.joohyuk.datarex.application.port.out.peresistence;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.List;
import java.util.Optional;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;

public interface TransformDocumentResultOutboxRepository {

  TransformDocumentResultOutbox save(TransformDocumentResultOutbox transformDocumentResultOutbox);

  List<TransformDocumentResultOutbox> findAllByOutboxStatus(
      String sagaType,
      OutboxStatus outboxStatus
  );

  Optional<TransformDocumentResultOutbox> findByCorrelationId(String correlationId);
}
