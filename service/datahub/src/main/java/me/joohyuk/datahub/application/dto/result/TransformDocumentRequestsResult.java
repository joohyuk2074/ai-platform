package me.joohyuk.datahub.application.dto.result;

import com.spartaecommerce.domain.vo.TrackingId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;

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

  public static TransformDocumentRequestsResult fromEvents(List<TransformDocumentEvent> events) {
    List<TrackingId> trackingIds = events.stream()
        .map(event -> new TrackingId(UUID.fromString(event.getTrackingId())))
        .toList();

    return new TransformDocumentRequestsResult(new ArrayList<>(trackingIds));
  }
}
