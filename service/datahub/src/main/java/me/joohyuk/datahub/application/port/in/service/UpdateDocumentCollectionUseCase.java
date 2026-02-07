package me.joohyuk.datahub.application.port.in.service;

import com.spartaecommerce.domain.vo.CollectionId;
import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;

public interface UpdateDocumentCollectionUseCase {

  CreateDocumentCollectionResult updateCollection(
      CollectionId collectionId,
      UpdateDocumentCollectionCommand command
  );
}
