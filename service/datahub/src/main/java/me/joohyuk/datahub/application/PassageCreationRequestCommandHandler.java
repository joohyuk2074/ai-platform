package me.joohyuk.datahub.application;

import com.spartaecommerce.util.DateTimeHolder;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.event.PassageCreationRequestedEvent;
import me.joohyuk.datahub.domain.exception.IngestionDomainException;
import me.joohyuk.datahub.domain.port.out.message.publisher.PassageCreationRequestPublisher;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.vo.CollectionId;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationRequestCommandHandler {

  private final DocumentRepository documentRepository;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final PassageCreationRequestPublisher passageCreationRequestPublisher;
  private final DateTimeHolder dateTimeHolder;

  @Transactional
  public int requestPassageCreationByCollection(CollectionId collectionId) {
    if (!documentCollectionRepository.existsById(collectionId)) {
      throw new IngestionDomainException(
          "Failed to find Collection. collectionId: " + collectionId.getValue());
    }

    List<Document> documents = documentRepository.findByCollectionId(collectionId);

    Instant now = dateTimeHolder.now();
    int publishedCount = 0;

    for (Document document : documents) {
      if (document.getStatus() != DocumentStatus.UPLOADED) {
        log.debug("Skipping document id={}: status={} (not UPLOADED)",
            document.getId().getValue(), document.getStatus());
        continue;
      }

      // UPLOADED → PASSAGE_REQUESTED 상태 전이
      document.requestPassageCreation(now);
      documentRepository.save(document);

      PassageCreationRequestedEvent event =
          new PassageCreationRequestedEvent(document, dateTimeHolder.getCurrentDateTime());
      passageCreationRequestPublisher.publish(event);

      publishedCount++;
    }

    log.info("Passage creation requested for collectionId={}: total={}, published={}",
        collectionId.getValue(), documents.size(), publishedCount);

    return publishedCount;
  }
}
