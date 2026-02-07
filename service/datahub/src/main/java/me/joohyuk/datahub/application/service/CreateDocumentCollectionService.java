package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.port.IdGenerator;
import com.spartaecommerce.domain.vo.UserId;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.application.dto.command.CreateDocumentCollectionCommand;
import me.joohyuk.datahub.application.dto.result.CreateDocumentCollectionResult;
import me.joohyuk.datahub.application.port.in.service.CreateDocumentCollectionUseCase;
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
public class CreateDocumentCollectionService implements CreateDocumentCollectionUseCase {

  private final IdGenerator idGenerator;
  private final DateTimeHolder dateTimeHolder;
  private final DocumentCollectionRepository documentCollectionRepository;

  @Override
  public CreateDocumentCollectionResult createCollection(
      UserId userId,
      CreateDocumentCollectionCommand command
  ) {
    log.info("Processing collection creation request: {}", command.name());

    if (documentCollectionRepository.existsByName(command.name())) {
      throw new DatahubDomainException(
          "Collection with name '" + command.name() + "' already exists",
          DatahubDomainErrorCode.COLLECTION_NAME_ALREADY_EXISTS
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
}
