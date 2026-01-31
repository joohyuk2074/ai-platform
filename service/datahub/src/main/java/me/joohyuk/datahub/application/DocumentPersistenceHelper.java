package me.joohyuk.datahub.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.service.DocumentDomainService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentPersistenceHelper {

  private final DocumentDomainService documentDomainService;
  private final DocumentRepository documentRepository;
//  private final IdGenerator idGenerator;
//  private final DateTimeHolder dateTimeHolder;

  @Transactional
  public DocumentUploadedEvent persistDocument(Document document) {
//    document.initialize(idGenerator.generateId(), dateTimeHolder.now());
    DocumentUploadedEvent event = documentDomainService.createDocument(document);
    Document savedDocument = documentRepository.save(document);
    log.info("Document saved to DB with id: {}", savedDocument.getId().getValue());
    return event;
  }
}
