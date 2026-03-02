package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.application.port.in.service.CreateDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.in.service.DeleteDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.in.service.UpdateDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubErrorCode;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCollectionCommandService implements
    CreateDocumentCollectionUseCase,
    UpdateDocumentCollectionUseCase,
    DeleteDocumentCollectionUseCase {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;
  private final DocumentCollectionRepository documentCollectionRepository;

  @Override
  public CreateDocumentCollectionResult createCollection(
      CreateDocumentCollectionCommand createCommand
  ) {
    log.info("Processing collection creation request: {}", createCommand.name());

    if (documentCollectionRepository.existsByName(createCommand.name())) {
      throw new DatahubDomainException(
          "Collection with name '" + createCommand.name() + "' already exists",
          DatahubErrorCode.COLLECTION_NAME_ALREADY_EXISTS
      );
    }

    Instant now = dateTimeHolder.now();
    DocumentCollection collection = DocumentCollection.of(
        idGenerator.generateId(),
        createCommand.name(),
        createCommand.description(),
        now,
        now
    );

    log.info("Document collection created successfully with ID: {}", collection.getId());

    DocumentCollection savedCollection = documentCollectionRepository.save(collection);
    log.info("Collection saved successfully with ID: {}", savedCollection.getId());

    return CreateDocumentCollectionResult.from(savedCollection);
  }

  @Override
  public CreateDocumentCollectionResult updateCollection(
      UpdateDocumentCollectionCommand updateCommand
  ) {
    log.info("Processing collection update request for ID: {}", updateCommand.collectionId());

    DocumentCollection collection =
        documentCollectionRepository.getById(updateCommand.collectionId());

    documentCollectionRepository.findByName(updateCommand.name())
        .ifPresent(existingCollection -> {
          if (!existingCollection.getId().equals(collection.getId())) {
            throw new DatahubDomainException(
                "Collection with name '" + updateCommand.name() + "' already exists",
                DatahubErrorCode.COLLECTION_NAME_ALREADY_EXISTS
            );
          }
        });

    Instant now = dateTimeHolder.now();
    collection.modify(updateCommand.name(), updateCommand.description(), now);

    DocumentCollection updatedCollection = documentCollectionRepository.save(collection);
    log.info("Collection updated successfully with ID: {}", updatedCollection.getId());

    return CreateDocumentCollectionResult.from(updatedCollection);
  }

  @Override
  public void deleteCollection(CollectionId collectionId) {
    DocumentCollection collection = documentCollectionRepository.findById(collectionId)
        .orElseThrow(() -> new DatahubDomainException(
            "Collection not found with ID: " + collectionId,
            DatahubErrorCode.DOCUMENT_COLLECTION_NOT_FOUND
        ));

    // TODO: collectionId에 해당하는 Document가 조회되는지 여부에따라서 삭제요청하도록 수정

    documentCollectionRepository.delete(collectionId);

    log.info("Collection deleted successfully with ID: {}", collectionId);
  }
}
