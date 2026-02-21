package me.joohyuk.datahub.infrastructure.adapter.in.web.dto;

import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;

public record DocumentTransformRequestResponse(
    String collectionId,
    int totalRequested
) {

  public static DocumentTransformRequestResponse of(
      TransformDocumentRequestsResult result,
      String collectionId
  ) {

    return new DocumentTransformRequestResponse(
        collectionId,
        result.transformTrackingIds().size()
    );
  }
}
