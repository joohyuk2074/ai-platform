package me.joohyuk.datahub.application.port.out.message.publisher;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;

public interface TransformDocumentMessagePublisher {

  void publish(
      TransformDocumentOutbox transformDocumentOutbox,
      BiConsumer<TransformDocumentOutbox, OutboxStatus> outboxCallback
  );
}
