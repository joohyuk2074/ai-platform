package me.joohyuk.datarex.domain.port.in.service;

import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;

public interface DocumentTransformService {

  void transformDocument(DocumentTransformRequestedMessage message);
}
