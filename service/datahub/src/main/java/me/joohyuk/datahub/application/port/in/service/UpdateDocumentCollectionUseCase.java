package me.joohyuk.datahub.application.port.in.service;

import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;

public interface UpdateDocumentCollectionUseCase {

  CreateDocumentCollectionResult updateCollection(UpdateDocumentCollectionCommand command);
}
