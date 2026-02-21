package me.joohyuk.datarex.application.port.out.message;

import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;

public interface DocumentTransformResultEventPublisher {

  void publishCompleted(TransformDocumentCompletedEvent message);
}
