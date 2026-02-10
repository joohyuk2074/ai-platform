package me.joohyuk.datahub.domain.event;

import java.time.Instant;
import me.joohyuk.datahub.domain.entity.Document;

public class DocumentUploadedEvent extends DocumentEvent {

  public DocumentUploadedEvent(Document document, Instant createdAt) {
    super(document, createdAt);
  }

  @Override
  public void fire() {

  }
}
