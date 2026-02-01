package me.joohyuk.datahub.application;

import com.spartaecommerce.domain.event.EmptyEvent;
import com.spartaecommerce.domain.vo.DocumentId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStep;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.entity.PassageResponse;
import me.joohyuk.datahub.domain.event.PassageCreationRequestEvent;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentCollectionRepository;
import me.joohyuk.datahub.domain.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.service.DocumentDomainService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationSaga implements
    SagaStep<PassageResponse, PassageCreationRequestEvent, EmptyEvent> {

  private final DocumentDomainService documentDomainService;
  private final DocumentCollectionRepository documentCollectionRepository;
  private final DocumentRepository documentRepository;

  // Passage 생성 실패인경우 passage 생성 요청을 롤백하면 됨
  // 로컬 데이터베이스 작업, saga 흐름은 멈출 것
  @Override
  @Transactional
  public PassageCreationRequestEvent process(PassageResponse passageResponse) {
    log.info("Completing passaging for document with id: {}", passageResponse.getDocumentId());
    Document document = documentRepository.getById(new DocumentId(passageResponse.getDocumentId()));
    PassageCreationRequestEvent domainEvent = documentDomainService.createPassage(document);

    documentRepository.save(document);

    log.info("Document with id: {} is created to passages", document.getId().getValue());

    return domainEvent;
  }

  @Override
  @Transactional
  public EmptyEvent rollback(PassageResponse passageResponse) {
    log.info("Cancelling with id: {}", passageResponse.getDocumentId());
    Document document = documentRepository.getById(new DocumentId(passageResponse.getDocumentId()));
    documentDomainService.cancelCreatePassage(document, passageResponse.getFailureMessages());

    documentRepository.save(document);

    log.info("Document with id: {} is cancelled", document.getId().getValue());

    return EmptyEvent.INSTANCE;
  }
}
