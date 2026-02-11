package me.joohyuk.datarex.application.port.in;

import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;

public interface DocumentTransformUseCase {

  void transformDocument(DocumentTransformRequestedMessage message);
}
