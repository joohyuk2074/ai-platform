package me.joohyuk.datahub.application.service.handler;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.TrackingId;
import com.spartaecommerce.exception.DomainException;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UploadDocumentCommand;
import me.joohyuk.datahub.application.dto.result.FileStorageResult;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.application.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.DocumentUploadedEvent;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadDocumentHandler {

  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final DateTimeHolder dateTimeHolder;
  private final IdGenerator idGenerator;

  @Transactional
  public DocumentUploadedEvent persistDocument(
      UploadDocumentCommand command,
      FileStorageResult storageResult,
      Metadata metadata
  ) {
    if (!documentCollectionRepository.existsById(command.collectionId())) {
      throw new DomainException(
          "Failed to find Collection. collectionId: " + command.collectionId().getValue(),
          DatahubErrorCode.DOCUMENT_COLLECTION_NOT_FOUND
      );
    }

    DocumentId documentId = new DocumentId(idGenerator.generateId());
    TrackingId trackingId = new TrackingId(UUID.randomUUID());
    Instant now = dateTimeHolder.now();

    Document document = Document.createForUpload(
        command.collectionId(),
        storageResult.fileKey(),
        storageResult.contentHash(),
        metadata,
        documentId,
        trackingId,
        now
    );

    log.info("Document created with documentId: {} and fileKey: {}",
        document.getId().getValue(),
        document.getFileKey()
    );

    Document savedDocument = documentRepository.save(document);

    log.info("Document saved: documentId={}, status={}",
        savedDocument.getId().getValue(), savedDocument.getStatus());

    return new DocumentUploadedEvent(savedDocument, now);
  }

  @Transactional
  public void saveAll(List<Document> documents) {
    if (!documents.isEmpty()) {
      documentRepository.saveAll(documents);
      log.info("Batch saved {} documents", documents.size());
    }
  }
}
