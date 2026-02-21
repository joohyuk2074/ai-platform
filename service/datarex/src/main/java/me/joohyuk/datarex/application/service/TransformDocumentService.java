package me.joohyuk.datarex.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.port.in.TransformDocumentUseCase;
import me.joohyuk.datarex.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datarex.application.service.handler.TransformDocumentResultOutboxHandler;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransformDocumentService implements TransformDocumentUseCase {

  private final TransformDocumentHandler transformDocumentHandler;
  private final TransformDocumentResultOutboxHandler transformDocumentResultOutboxHandler;

  @Override
  public void transformDocument(TransformDocumentCommand command) {
    TransformDocumentCompletedEvent documentCompletedEvent =
        transformDocumentHandler.transform(command);

    transformDocumentResultOutboxHandler.save(documentCompletedEvent);
  }
}
