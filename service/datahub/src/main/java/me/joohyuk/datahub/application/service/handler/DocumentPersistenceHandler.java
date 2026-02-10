package me.joohyuk.datahub.application.service.handler;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.exception.DomainException;
import com.spartaecommerce.util.DateTimeHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.application.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.service.DocumentDomainService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentPersistenceHandler {

  private final DocumentDomainService documentDomainService;
  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final DateTimeHolder dateTimeHolder;
  private final IdGenerator idGenerator;

  @Transactional
  public DocumentUploadedEvent persistDocument(Document document) {
    if (!documentCollectionRepository.existsById(document.getCollectionId())) {
      throw new DomainException(
          "Failed to find Collection. collectionId: " + document.getCollectionId().getValue(),
          DatahubErrorCode.DOCUMENT_COLLECTION_NOT_FOUND
      );
    }

    DocumentUploadedEvent uploadEvent = documentDomainService.upload(
        new DocumentId(idGenerator.generateId()),
        document,
        dateTimeHolder.now()
    );

    Document savedDocument = documentRepository.save(document);

    log.info("Document saved: documentId={}, status={}",
        savedDocument.getId().getValue(), savedDocument.getStatus());

    return uploadEvent;
  }

  @Transactional
  public void saveAll(List<Document> documents) {
    if (!documents.isEmpty()) {
      documentRepository.saveAll(documents);
      log.info("Batch saved {} documents", documents.size());
    }
  }
}
