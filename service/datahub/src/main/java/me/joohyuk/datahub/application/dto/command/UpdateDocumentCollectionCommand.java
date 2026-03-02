package me.joohyuk.datahub.application.dto.command;

import com.spartaecommerce.domain.vo.CollectionId;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;

public record UpdateDocumentCollectionCommand(
    CollectionId collectionId,
    String name,
    String description
) {

  public UpdateDocumentCollectionCommand {
    if (name == null || name.isBlank()) {
      throw new DatahubDomainException(
          "Collection name cannot be empty",
          DatahubErrorCode.INVALID_COLLECTION_NAME
      );
    }
  }
}
