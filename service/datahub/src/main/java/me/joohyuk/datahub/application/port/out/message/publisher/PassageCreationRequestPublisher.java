package me.joohyuk.datahub.application.port.out.message.publisher;

import me.joohyuk.datahub.domain.event.TransformDocumentEvent;

public interface PassageCreationRequestPublisher {

  void publish(TransformDocumentEvent event);
}
