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

  /**
   * Document Transform 성공 처리.
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORMED로 전이시킵니다.
   * 로컬 데이터베이스 작업이며, saga 흐름은 여기서 완료됩니다.
   */
  @Override
  @Transactional
  public PassageCreationRequestEvent process(PassageResponse passageResponse) {
    log.info("Processing transform completion for documentId: {}", passageResponse.getDocumentId());

    Document document = documentRepository.getById(new DocumentId(passageResponse.getDocumentId()));
    PassageCreationRequestEvent domainEvent = documentDomainService.createPassage(
        document,
        passageResponse.getPassageCount(),
        passageResponse.getEventId()
    );

    documentRepository.save(document);

    log.info("Document with documentId: {} transformed successfully with {} passages",
        document.getId().getValue(), passageResponse.getPassageCount());

    return domainEvent;
  }

  /**
   * Document Transform 실패 처리 (롤백).
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORM_FAILED로 전이시킵니다.
   * 로컬 데이터베이스 작업이며, saga 흐름은 여기서 멈춥니다.
   */
  @Override
  @Transactional
  public EmptyEvent rollback(PassageResponse passageResponse) {
    log.info("Rolling back transform for documentId: {}", passageResponse.getDocumentId());

    Document document = documentRepository.getById(new DocumentId(passageResponse.getDocumentId()));
    documentDomainService.cancelCreatePassage(
        document,
        passageResponse.getErrorCode(),
        passageResponse.getErrorMessage(),
        passageResponse.getEventId()
    );

    documentRepository.save(document);

    log.info("Document with documentId: {} transform failed with error: {}",
        document.getId().getValue(), passageResponse.getErrorCode());

    return EmptyEvent.INSTANCE;
  }
}
