package me.joohyuk.datahub.domain.port.in.service;

import com.spartaecommerce.domain.vo.UserId;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import com.spartaecommerce.domain.vo.CollectionId;

public interface DocumentCollectionCommandService {

  CreateDocumentCollectionResult createCollection(
      UserId userId,
      CreateDocumentCollectionCommand command
  );

  CreateDocumentCollectionResult updateCollection(
      CollectionId collectionId,
      UpdateDocumentCollectionCommand command
  );

  void deleteCollection(CollectionId collectionId);
}
