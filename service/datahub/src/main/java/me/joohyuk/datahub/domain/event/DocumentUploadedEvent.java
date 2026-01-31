package me.joohyuk.datahub.domain.event;

import java.time.LocalDateTime;
import me.joohyuk.datahub.domain.entity.Document;

public class DocumentUploadedEvent extends DocumentEvent {

  public DocumentUploadedEvent(Document document, LocalDateTime createdAt) {
    super(document, createdAt);
  }
}
