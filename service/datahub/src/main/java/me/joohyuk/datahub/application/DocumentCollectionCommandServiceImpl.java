package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.request.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.request.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.response.CreateDocumentCollectionResult;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.in.service.DocumentCollectionCommandService;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.vo.CollectionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCollectionCommandServiceImpl implements DocumentCollectionCommandService {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;
  private final DocumentCollectionRepository documentCollectionRepository;

  @Override
  public CreateDocumentCollectionResult createCollection(CreateDocumentCollectionCommand command) {
    log.info("Processing collection creation request: {}", command.name());

    if (documentCollectionRepository.existsByName(command.name())) {
      throw new IngestionDomainException(
          "Collection with name '" + command.name() + "' already exists"
      );
    }

    Instant now = dateTimeHolder.now();
    DocumentCollection collection = DocumentCollection.of(
        idGenerator.generateId(),
        command.name(),
        command.description(),
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
      CollectionId collectionId,
      UpdateDocumentCollectionCommand command
  ) {
    log.info("Processing collection update request for ID: {}", collectionId);

    DocumentCollection collection = documentCollectionRepository.getById(collectionId);

    documentCollectionRepository.findByName(command.name())
        .ifPresent(existingCollection -> {
          if (!existingCollection.getId().equals(collection.getId())) {
            throw new IngestionDomainException(
                "Collection with name '" + command.name() + "' already exists"
            );
          }
        });

    Instant now = dateTimeHolder.now();
    collection.modify(command.name(), command.description(), now);

    DocumentCollection updatedCollection = documentCollectionRepository.save(collection);
    log.info("Collection updated successfully with ID: {}", updatedCollection.getId());

    return CreateDocumentCollectionResult.from(updatedCollection);
  }

  @Override
  public void deleteCollection(CollectionId collectionId) {
    DocumentCollection collection = documentCollectionRepository.findById(collectionId)
        .orElseThrow(() -> new IngestionDomainException(
            "Collection not found with ID: " + collectionId
        ));

    // TODO: collectionId에 해당하는 Document가 조회되는지 여부에따라서 삭제요청하도록 수정

    documentCollectionRepository.delete(collectionId);

    log.info("Collection deleted successfully with ID: {}", collectionId);
  }
}
