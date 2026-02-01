package me.joohyuk.datahub.domain.event;

import java.time.LocalDateTime;
import me.joohyuk.datahub.domain.entity.Document;

public class PassageCreationRequestedEvent extends DocumentEvent {

  public PassageCreationRequestedEvent(
      Document document,
      LocalDateTime createdAt
  ) {
    super(document, createdAt);
  }
}
