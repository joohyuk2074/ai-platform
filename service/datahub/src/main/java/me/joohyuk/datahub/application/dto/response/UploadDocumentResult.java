package me.joohyuk.datahub.application.dto.response;

import me.joohyuk.datahub.domain.entity.Document;

public record UploadDocumentResult(
    Long documentId
) {

  public static UploadDocumentResult from(Document document) {
    return new UploadDocumentResult(document.getId().getValue());
  }
}
