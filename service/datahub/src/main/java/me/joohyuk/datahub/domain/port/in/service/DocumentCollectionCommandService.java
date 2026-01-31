package me.joohyuk.datahub.domain.port.in.service;

import me.joohyuk.datahub.application.dto.request.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.request.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.response.CreateDocumentCollectionResult;
import me.joohyuk.datahub.domain.vo.CollectionId;

public interface DocumentCollectionCommandService {

  CreateDocumentCollectionResult createCollection(CreateDocumentCollectionCommand command);

  CreateDocumentCollectionResult updateCollection(
      CollectionId collectionId,
      UpdateDocumentCollectionCommand command
  );

  void deleteCollection(CollectionId collectionId);
}
