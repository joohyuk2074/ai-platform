package me.joohyuk.datahub.domain.service;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.util.DateTimeHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentDomainService {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;

  public DocumentUploadedEvent initializeDocument(Document document) {
    document.initialize(idGenerator.generateId(), dateTimeHolder.now());

    log.info("Document created with documentId: {} and fileKey: {}", document.getId().getValue(),
        document.getFileKey());

    return new DocumentUploadedEvent(document, dateTimeHolder.getCurrentDateTime());
  }

  public PassageCreationRequestEvent createPassage(Document document) {
    return null;
  }

  public void cancelCreatePassage(Document document, List<String> failureMessages) {
  }
}
