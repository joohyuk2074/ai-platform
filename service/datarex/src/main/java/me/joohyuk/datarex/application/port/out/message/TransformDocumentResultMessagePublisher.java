package me.joohyuk.datarex.application.port.out.message;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;

public interface TransformDocumentResultMessagePublisher {

  void publish(
      TransformDocumentResultOutbox resultOutbox,
      BiConsumer<TransformDocumentResultOutbox, OutboxStatus> outboxCallback
  );
}
