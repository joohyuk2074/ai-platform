package me.joohyuk.datahub.domain.event;

import java.time.LocalDateTime;
import me.joohyuk.datahub.domain.entity.Document;

public class PassageCreationRequestEvent extends DocumentEvent {

  public PassageCreationRequestEvent(
      Document document,
      LocalDateTime createdAt
  ) {
    super(document, createdAt);
  }

  @Override
  public void fire() {

  }
}
