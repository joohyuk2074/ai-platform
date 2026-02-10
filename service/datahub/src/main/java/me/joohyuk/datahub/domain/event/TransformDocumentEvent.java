package me.joohyuk.datahub.domain.event;

import java.time.Instant;
import me.joohyuk.datahub.domain.entity.Document;

public class TransformDocumentEvent extends DocumentEvent {

  public TransformDocumentEvent(
      Document document,
      Instant createdAt
  ) {
    super(document, createdAt);
  }

  public static TransformDocumentEvent of(Document document, Instant createdAt) {
    return new TransformDocumentEvent(document, createdAt);
  }

  @Override
  public void fire() {

  }
}
