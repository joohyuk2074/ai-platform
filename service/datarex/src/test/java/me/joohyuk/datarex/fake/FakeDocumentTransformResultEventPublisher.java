package me.joohyuk.datarex.fake;

import com.spartaecommerce.outbox.OutboxStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import me.joohyuk.datarex.application.port.out.message.TransformDocumentResultMessagePublisher;
import me.joohyuk.datarex.domain.event.TransformDocumentResultOutbox;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;

/**
 * Fake implementation of DocumentTransformResultEventPublisher for testing.
 * <p>
 * This fake allows tests to: - Capture all published events for verification - Verify event
 * ordering and content - Check which events were published
 */
public class FakeDocumentTransformResultEventPublisher implements
    TransformDocumentResultMessagePublisher {

  private final List<TransformDocumentCompletedEvent> completedMessages = new ArrayList<>();

  @Override
  public void publish(
      TransformDocumentResultOutbox resultOutbox,
      BiConsumer<TransformDocumentResultOutbox, OutboxStatus> outboxCallback
  ) {

  }
}
