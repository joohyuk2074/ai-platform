package me.joohyuk.datahub.domain.event;

import com.spartaecommerce.domain.event.DomainEvent;
import java.time.LocalDateTime;
import lombok.Getter;
import me.joohyuk.datahub.domain.entity.Document;

@Getter
public abstract class DocumentEvent implements DomainEvent<Document> {

  private final Document document;
  private final LocalDateTime createdAt;

  protected DocumentEvent(Document document, LocalDateTime createdAt) {
    this.document = document;
    this.createdAt = createdAt;
  }
}
