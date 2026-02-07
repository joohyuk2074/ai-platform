package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.UpdateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.application.port.in.service.UpdateDocumentCollectionUseCase;
import me.joohyuk.datahub.application.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.exception.DatahubDomainErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateDocumentCollectionService implements UpdateDocumentCollectionUseCase {

  private final DateTimeHolder dateTimeHolder;
  private final DocumentCollectionRepository documentCollectionRepository;

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
            throw new DatahubDomainException(
                "Collection with name '" + command.name() + "' already exists",
                DatahubDomainErrorCode.COLLECTION_NAME_ALREADY_EXISTS
            );
          }
        });

    Instant now = dateTimeHolder.now();
    collection.modify(command.name(), command.description(), now);

    DocumentCollection updatedCollection = documentCollectionRepository.save(collection);
    log.info("Collection updated successfully with ID: {}", updatedCollection.getId());

    return CreateDocumentCollectionResult.from(updatedCollection);
  }
}
