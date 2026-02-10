package me.joohyuk.datahub.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;
import me.joohyuk.datahub.domain.entity.Document;

@Getter
public abstract class DocumentEvent implements DomainEvent<Document> {

  private final Document document;
  private final Instant createdAt;

  protected DocumentEvent(Document document, Instant createdAt) {
    this.document = document;
    this.createdAt = createdAt;
  }
}
