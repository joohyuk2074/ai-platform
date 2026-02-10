package me.joohyuk.datahub.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;

public record DocumentTransformRequestResponse(
    String collectionId,
    int totalRequested,
    Instant requestedAt
) {

  public static DocumentTransformRequestResponse of(
      TransformDocumentRequestsResult result,
      String collectionId,
      Instant requestedAt
  ) {

    return new DocumentTransformRequestResponse(
        collectionId,
        result.transformTrackingIds().size(),
        requestedAt
    );
  }
}
