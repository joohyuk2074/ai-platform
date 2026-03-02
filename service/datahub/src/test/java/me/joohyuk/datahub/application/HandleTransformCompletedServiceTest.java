package me.joohyuk.datahub.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.TrackingId;
import com.spartaecommerce.outbox.OutboxStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import me.joohyuk.datahub.application.service.HandleTransformCompletedService;
import me.joohyuk.datahub.application.service.handler.DlqOutboxHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import me.joohyuk.datahub.fake.FakeDateTimeHolder;
import me.joohyuk.datahub.fake.InMemoryDlqOutboxRepository;
import me.joohyuk.datahub.fake.InMemoryDocumentCollectionRepository;
import me.joohyuk.datahub.fake.InMemoryDocumentRepository;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import me.joohyuk.datahub.fake.InMemoryTransformDocumentOutboxRepository;
import me.joohyuk.datahub.infrastructure.adapter.in.listener.dto.TransformDocumentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * HandleTransformCompletedService 고전파(Classical School) 테스트
 *
 * <p>Mock 대신 Fake 구현체를 사용하여 실제 객체 그래프를 구성합니다.
 * <ul>
 *   <li>InMemoryTransformDocumentOutboxRepository: Outbox 저장소 대체</li>
 *   <li>InMemoryDocumentRepository: 문서 저장소 대체</li>
 *   <li>InMemoryDlqOutboxRepository: DLQ Outbox 저장소 대체</li>
 *   <li>FakeDateTimeHolder: 시간 제어</li>
 *   <li>실제 핸들러들 사용</li>
 * </ul>
 *
 * <p>검증 방식:
 * <ul>
 *   <li>출력 기반 검증: 메서드의 반환값이 올바른가? (이 경우 void이므로 적용 안 됨)</li>
 *   <li>상태 기반 검증: Repository의 상태가 올바르게 변경되었는가?</li>
 * </ul>
 *
 * <p>우선순위:
 * <ol>
 *   <li>가독성: 명확한 Given-When-Then 구조</li>
 *   <li>리팩터링 내성: 구현 세부사항이 아닌 최종 결과 검증</li>
 *   <li>실행 속도: In-memory 구현으로 빠른 실행</li>
 *   <li>회귀 방지: 중요 시나리오의 올바른 동작 보장</li>
 * </ol>
 */
@DisplayName("HandleTransformCompletedService 테스트")
class HandleTransformCompletedServiceTest {

  private static final CollectionId COLLECTION_ID = new CollectionId(1L);
  private static final DocumentId DOCUMENT_ID = new DocumentId(100L);
  private static final String CORRELATION_ID = "test-correlation-123";
  private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");

  private HandleTransformCompletedService service;
  private InMemoryTransformDocumentOutboxRepository outboxRepository;
  private InMemoryDocumentRepository documentRepository;
  private InMemoryDlqOutboxRepository dlqOutboxRepository;
  private FakeDateTimeHolder dateTimeHolder;

  @BeforeEach
  void setUp() {
    // Classical School: 실제 객체 그래프 구성
    var idGenerator = new InMemoryIdGenerator();
    dateTimeHolder = new FakeDateTimeHolder(FIXED_TIME);
    outboxRepository = new InMemoryTransformDocumentOutboxRepository();
    documentRepository = new InMemoryDocumentRepository();
    dlqOutboxRepository = new InMemoryDlqOutboxRepository();

    // Collection 존재 체크를 위해 사전 시드
    var collectionRepository = new InMemoryDocumentCollectionRepository();
    collectionRepository.save(
        DocumentCollection.of(COLLECTION_ID.getValue(), "test-collection", "desc",
            Instant.now(), Instant.now()));

    TransformDocumentHandler transformHandler = new TransformDocumentHandler(
        documentRepository,
        collectionRepository,
        dateTimeHolder
    );

    DlqOutboxHandler dlqOutboxHandler = new DlqOutboxHandler(
        dlqOutboxRepository,
        idGenerator,
        dateTimeHolder
    );

    service = new HandleTransformCompletedService(
        outboxRepository,
        transformHandler,
        dlqOutboxHandler,
        dateTimeHolder
    );
  }

  @Nested
  @DisplayName("handleCompleted 메서드는")
  class HandleCompletedTests {

    @Test
    @DisplayName("Outbox가 없으면 조기 반환하고 아무 작업도 수행하지 않는다")
    void should_return_early_when_outbox_not_found() {
      // Given: Outbox가 존재하지 않음
      TransformDocumentCompletedEvent event = createSuccessEvent(CORRELATION_ID, DOCUMENT_ID, 10);

      // When
      service.handleCompleted(event);

      // Then: 상태 기반 검증 - 아무 작업도 수행되지 않음
      assertThat(outboxRepository.size()).isEqualTo(0);
      assertThat(documentRepository.size()).isEqualTo(0);
      assertThat(dlqOutboxRepository.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("변환 성공 시 Document를 TRANSFORMED로 업데이트하고 Outbox를 완료 처리한다")
    void should_complete_transform_when_success() {
      // Given: Document와 Outbox가 존재
      Document document = createTransformRequestedDocument();
      documentRepository.save(document);

      TransformDocumentOutbox outbox = createPendingOutbox(CORRELATION_ID);
      outboxRepository.save(outbox);

      TransformDocumentCompletedEvent event = createSuccessEvent(CORRELATION_ID, DOCUMENT_ID, 10);

      // When
      service.handleCompleted(event);

      // Then: 상태 기반 검증 - Document가 TRANSFORMED로 변경됨
      Document updatedDocument = documentRepository.getById(DOCUMENT_ID);
      assertThat(updatedDocument.getStatus()).isEqualTo(DocumentStatus.TRANSFORMED);
      assertThat(updatedDocument.getPassageCount()).isEqualTo(10);
      assertThat(updatedDocument.getLastResultEventId()).isEqualTo(CORRELATION_ID);

      // And: Outbox가 완료 처리됨
      TransformDocumentOutbox updatedOutbox = outboxRepository.findById(outbox.getId());
      assertThat(updatedOutbox.getProcessedAt()).isNotNull();
      assertThat(updatedOutbox.getProcessedAt()).isEqualTo(toLocalDateTime(FIXED_TIME));
    }

    @Test
    @DisplayName("재시도 가능한 에러 발생 시 Document를 FAILED로 업데이트하고 Outbox를 PENDING으로 되돌린다")
    void should_revert_outbox_to_pending_when_retryable_error() {
      // Given: Document와 Outbox가 존재
      Document document = createTransformRequestedDocument();
      documentRepository.save(document);

      TransformDocumentOutbox outbox = createPendingOutbox(CORRELATION_ID);
      outboxRepository.save(outbox);

      TransformDocumentCompletedEvent event = createFailureEvent(
          CORRELATION_ID, DOCUMENT_ID, "NETWORK_ERROR", "Network timeout");

      // When
      service.handleCompleted(event);

      // Then: 상태 기반 검증 - Document가 FAILED로 변경됨
      Document updatedDocument = documentRepository.getById(DOCUMENT_ID);
      assertThat(updatedDocument.getStatus()).isEqualTo(DocumentStatus.TRANSFORM_FAILED);
      assertThat(updatedDocument.getLastErrorCode()).isEqualTo("NETWORK_ERROR");
      assertThat(updatedDocument.getLastErrorMessage()).isEqualTo("Network timeout");
      assertThat(updatedDocument.getAttempt()).isEqualTo(1);

      // And: Outbox가 PENDING으로 되돌아감 (재시도 가능)
      TransformDocumentOutbox updatedOutbox = outboxRepository.findById(outbox.getId());
      assertThat(updatedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);

      // And: DLQ에 저장되지 않음
      assertThat(dlqOutboxRepository.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("영구적 에러 발생 시 Document를 FAILED로 업데이트하고 DLQ에 저장하며 Outbox를 FAILED로 마킹한다")
    void should_send_to_dlq_when_permanent_error() {
      // Given: Document와 Outbox가 존재
      Document document = createTransformRequestedDocument();
      documentRepository.save(document);

      TransformDocumentOutbox outbox = createPendingOutbox(CORRELATION_ID);
      outboxRepository.save(outbox);

      TransformDocumentCompletedEvent event = createFailureEvent(
          CORRELATION_ID, DOCUMENT_ID, "INVALID_FORMAT", "Invalid file format");

      // When
      service.handleCompleted(event);

      // Then: 상태 기반 검증 - Document가 FAILED로 변경됨
      Document updatedDocument = documentRepository.getById(DOCUMENT_ID);
      assertThat(updatedDocument.getStatus()).isEqualTo(DocumentStatus.TRANSFORM_FAILED);
      assertThat(updatedDocument.getLastErrorCode()).isEqualTo("INVALID_FORMAT");
      assertThat(updatedDocument.getLastErrorMessage()).isEqualTo("Invalid file format");

      // And: Outbox가 FAILED로 마킹됨
      TransformDocumentOutbox updatedOutbox = outboxRepository.findById(outbox.getId());
      assertThat(updatedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
      assertThat(updatedOutbox.getProcessedAt()).isEqualTo(toLocalDateTime(FIXED_TIME));

      // And: DLQ에 저장됨
      assertThat(dlqOutboxRepository.size()).isEqualTo(1);
      var dlqOutbox = dlqOutboxRepository.findAll().get(0);
      assertThat(dlqOutbox.getCorrelationId()).isEqualTo(CORRELATION_ID);
      assertThat(dlqOutbox.getErrorCode()).isEqualTo("INVALID_FORMAT");
      assertThat(dlqOutbox.getErrorMessage()).isEqualTo("Invalid file format");
      assertThat(dlqOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("알 수 없는 에러 발생 시 안전하게 DLQ로 전송하고 Outbox를 FAILED로 마킹한다")
    void should_send_to_dlq_when_unknown_error() {
      // Given: Document와 Outbox가 존재
      Document document = createTransformRequestedDocument();
      documentRepository.save(document);

      TransformDocumentOutbox outbox = createPendingOutbox(CORRELATION_ID);
      outboxRepository.save(outbox);

      TransformDocumentCompletedEvent event = createFailureEvent(
          CORRELATION_ID, DOCUMENT_ID, "UNKNOWN_ERROR", "Something went wrong");

      // When
      service.handleCompleted(event);

      // Then: 상태 기반 검증 - Document가 FAILED로 변경됨
      Document updatedDocument = documentRepository.getById(DOCUMENT_ID);
      assertThat(updatedDocument.getStatus()).isEqualTo(DocumentStatus.TRANSFORM_FAILED);
      assertThat(updatedDocument.getLastErrorCode()).isEqualTo("UNKNOWN_ERROR");

      // And: Outbox가 FAILED로 마킹됨
      TransformDocumentOutbox updatedOutbox = outboxRepository.findById(outbox.getId());
      assertThat(updatedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);

      // And: DLQ에 저장됨
      assertThat(dlqOutboxRepository.size()).isEqualTo(1);
      var dlqOutbox = dlqOutboxRepository.findAll().get(0);
      assertThat(dlqOutbox.getCorrelationId()).isEqualTo(CORRELATION_ID);
      assertThat(dlqOutbox.getErrorCode()).isEqualTo("UNKNOWN_ERROR");
    }

    @Test
    @DisplayName("처리 중 예외 발생 시 Outbox를 FAILED로 마킹하고 예외를 던진다")
    void should_mark_outbox_failed_and_throw_exception_when_error_occurs() {
      // Given: Outbox는 존재하지만 Document가 존재하지 않아 예외 발생 예상
      TransformDocumentOutbox outbox = createPendingOutbox(CORRELATION_ID);
      outboxRepository.save(outbox);

      TransformDocumentCompletedEvent event = createSuccessEvent(CORRELATION_ID, DOCUMENT_ID, 10);

      // When & Then: 예외가 발생함
      assertThatThrownBy(() -> service.handleCompleted(event))
          .isInstanceOf(DatahubDomainException.class);

      // And: 상태 기반 검증 - Outbox가 FAILED로 마킹됨
      TransformDocumentOutbox updatedOutbox = outboxRepository.findById(outbox.getId());
      assertThat(updatedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
      assertThat(updatedOutbox.getProcessedAt()).isEqualTo(toLocalDateTime(FIXED_TIME));
    }

    @Test
    @DisplayName("여러 재시도 가능한 에러를 연속으로 처리하면 Document의 attempt가 증가한다")
    void should_increment_attempt_when_multiple_retryable_errors() {
      // Given: Document와 Outbox가 존재
      Document document = createTransformRequestedDocument();
      documentRepository.save(document);

      // First failure
      TransformDocumentOutbox outbox1 = createPendingOutbox(CORRELATION_ID + "-1");
      outboxRepository.save(outbox1);

      TransformDocumentCompletedEvent event1 = createFailureEvent(
          CORRELATION_ID + "-1", DOCUMENT_ID, "TIMEOUT", "Timeout error");

      service.handleCompleted(event1);

      // Document를 다시 TRANSFORM_REQUESTED로 변경 (재시도 시뮬레이션)
      Document doc = documentRepository.getById(DOCUMENT_ID);
      documentRepository.save(Document.restore(
          doc.getId(), doc.getCollectionId(), doc.getFileKey(), doc.getContentHash(),
          doc.getMetadata(), doc.getTrackingId(), DocumentStatus.TRANSFORM_REQUESTED,
          doc.getAttempt(), doc.getLastErrorCode(), doc.getLastErrorMessage(),
          doc.getPassageCount(), doc.getLastResultEventId(), doc.getCreatedAt(), doc.getUpdatedAt()
      ));

      // When: Second failure
      TransformDocumentOutbox outbox2 = createPendingOutbox(CORRELATION_ID + "-2");
      outboxRepository.save(outbox2);

      TransformDocumentCompletedEvent event2 = createFailureEvent(
          CORRELATION_ID + "-2", DOCUMENT_ID, "NETWORK_ERROR", "Network error");

      service.handleCompleted(event2);

      // Then: 상태 기반 검증 - attempt가 2로 증가
      Document updatedDocument = documentRepository.getById(DOCUMENT_ID);
      assertThat(updatedDocument.getAttempt()).isEqualTo(2);
      assertThat(updatedDocument.getStatus()).isEqualTo(DocumentStatus.TRANSFORM_FAILED);
    }
  }

  // ─── 테스트 헬퍼 메서드 ──────────────────────────────────────────────

  private Document createTransformRequestedDocument() {
    Document document = Document.createForUpload(
        COLLECTION_ID,
        "test-file.pdf",
        ContentHash.of("a".repeat(64)), // Valid 64-char SHA-256 hex string
        Metadata.forUpload("test.pdf", 1024L, "application/pdf", 1L),
        DOCUMENT_ID,
        new TrackingId(UUID.randomUUID()),
        FIXED_TIME
    );

    // UPLOADED -> TRANSFORM_REQUESTED로 전이
    document.transform(FIXED_TIME);

    return document;
  }

  private TransformDocumentOutbox createPendingOutbox(String correlationId) {
    return TransformDocumentOutbox.createPending(
        1L,
        correlationId,
        "{\"documentId\": 100}",
        DocumentStatus.TRANSFORM_REQUESTED,
        toLocalDateTime(FIXED_TIME)
    );
  }

  private TransformDocumentCompletedEvent createSuccessEvent(
      String correlationId,
      DocumentId documentId,
      int passageCount
  ) {
    return new TransformDocumentCompletedEvent(
        correlationId,
        String.valueOf(COLLECTION_ID.getValue()),
        String.valueOf(documentId.getValue()),
        "a".repeat(64),
        passageCount,
        null,
        null,
        FIXED_TIME
    );
  }

  private TransformDocumentCompletedEvent createFailureEvent(
      String correlationId,
      DocumentId documentId,
      String errorCode,
      String errorMessage
  ) {
    return new TransformDocumentCompletedEvent(
        correlationId,
        String.valueOf(COLLECTION_ID.getValue()),
        String.valueOf(documentId.getValue()),
        "a".repeat(64),
        0,
        errorCode,
        errorMessage,
        FIXED_TIME
    );
  }

  private LocalDateTime toLocalDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
  }
}
