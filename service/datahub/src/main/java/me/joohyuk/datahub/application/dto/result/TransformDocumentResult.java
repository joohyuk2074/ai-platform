package me.joohyuk.datahub.application.dto.result;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.DocumentId;
import java.time.Instant;
import java.util.List;

public record TransformDocumentResult(
    CollectionId collectionId,
    int totalDocumentsFound,
    int successfullyRequested,
    Instant requestedAt,
    List<DocumentRequestResult> documentResults
) {

  public boolean isAllRequestPublished() {
    return totalDocumentsFound == successfullyRequested;
  }

  public record DocumentRequestResult(
      DocumentId documentId,
      boolean success,
      String errorMessage
  ) {

    public static DocumentRequestResult success(DocumentId documentId) {
      return new DocumentRequestResult(documentId, true, null);
    }

    public static DocumentRequestResult failure(DocumentId documentId, String errorMessage) {
      return new DocumentRequestResult(documentId, false, errorMessage);
    }
  }
}
