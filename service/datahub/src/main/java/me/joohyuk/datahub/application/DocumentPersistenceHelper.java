package me.joohyuk.datahub.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
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
  private final DocumentCollectionRepository documentCollectionRepository;

  @Transactional
  public DocumentUploadedEvent persistDocument(Document document) {
    if (!documentCollectionRepository.existsById(document.getCollectionId())) {
      throw new IngestionDomainException(
          "Failed to find Collection. collectionId: " + document.getCollectionId().getValue());
    }

    DocumentUploadedEvent event = documentDomainService.createDocument(document);
    Document savedDocument = documentRepository.save(document);
    log.info("Document saved to DB with id: {}", savedDocument.getId().getValue());
    return event;
  }
}
