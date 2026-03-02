package me.joohyuk.datahub.application.port.in.service;

import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;

public interface CreateDocumentCollectionUseCase {

  CreateDocumentCollectionResult createCollection(CreateDocumentCollectionCommand command);
}
