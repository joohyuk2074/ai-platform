package me.joohyuk.datahub.application.port.in.service;

import me.joohyuk.datahub.infrastructure.adapter.in.listener.dto.TransformDocumentCompletedEvent;

public interface HandleTransformCompletedUseCase {

  void handleCompleted(TransformDocumentCompletedEvent event);
}
