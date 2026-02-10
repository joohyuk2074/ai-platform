package me.joohyuk.datahub.application.dto.result;

import com.spartaecommerce.domain.vo.TrackingId;
import java.util.ArrayList;
import java.util.List;
import me.joohyuk.datahub.domain.entity.Document;

public record TransformDocumentRequestsResult(
    List<TrackingId> transformTrackingIds
) {

  public static TransformDocumentRequestsResult empty() {
    return new TransformDocumentRequestsResult(List.of());
  }

  public static TransformDocumentRequestsResult from(List<Document> documents) {
    List<TrackingId> trackingIds = documents.stream()
        .map(Document::getTrackingId)
        .toList();

    return new TransformDocumentRequestsResult(new ArrayList<>(trackingIds));
  }
}
