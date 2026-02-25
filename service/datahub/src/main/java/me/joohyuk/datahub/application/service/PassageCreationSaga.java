package me.joohyuk.datahub.application.service;

import com.spartaecommerce.domain.event.EmptyEvent;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.util.DateTimeHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.joohyuk.commonsaga.SagaStep;
import me.joohyuk.datahub.application.port.out.persistence.DocumentRepository;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.entity.PassageResponse;
import me.joohyuk.datahub.domain.event.TransformDocumentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassageCreationSaga implements
    SagaStep<PassageResponse, TransformDocumentEvent, EmptyEvent> {

  private final DocumentRepository documentRepository;
  private final DateTimeHolder dateTimeHolder;

  /**
   * Document Transform 성공 처리.
   *
   * <p>Document 상태를 TRANSFORM_REQUESTED → TRANSFORMED로 전이시킵니다.
   * 로컬 데이터베이스 작업이며, saga 흐름은 여기서 완료됩니다.
   */
  @Override
  @Transactional
  public TransformDocumentEvent process(PassageResponse passageResponse) {
    log.info("Processing transform completion for documentId: {}", passageResponse.getDocumentId());

    Document document = documentRepository.getById(new DocumentId(passageResponse.getDocumentId()));
    document.completeTransform(
        passageResponse.getPassageCount(),
        passageResponse.getEventId(),
        dateTimeHolder.now()
    );

    log.info("Document transformed successfully: documentId={}, passageCount={}",
        document.getId().getValue(), passageResponse.getPassageCount());

    documentRepository.save(document);

    log.info("Document with documentId: {} transformed successfully with {} passages",
        document.getId().getValue(), passageResponse.getPassageCount());

    // TODO: 추후 Passage 처리 이벤트로 변경 필요
    return null;
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
    document.failTransform(
        passageResponse.getErrorCode(),
        passageResponse.getErrorMessage(),
        passageResponse.getEventId(),
        dateTimeHolder.now()
    );

    log.warn("Document transform failed: documentId={}, errorCode={}, errorMessage={}",
        document.getId().getValue(), passageResponse.getErrorCode(), passageResponse.getErrorMessage());

    documentRepository.save(document);

    log.info("Document with documentId: {} transform failed with error: {}",
        document.getId().getValue(), passageResponse.getErrorCode());

    return EmptyEvent.INSTANCE;
  }
}
