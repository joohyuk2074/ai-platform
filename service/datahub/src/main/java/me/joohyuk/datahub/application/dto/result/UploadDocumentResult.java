package me.joohyuk.datahub.application.dto.result;

import me.joohyuk.datahub.domain.entity.Document;

public record UploadDocumentResult(
    Long documentId,
    String fileKey
) {

  public static UploadDocumentResult from(Document document) {
    return new UploadDocumentResult(
        document.getId().getValue(),
        document.getFileKey()
    );
  }
}
