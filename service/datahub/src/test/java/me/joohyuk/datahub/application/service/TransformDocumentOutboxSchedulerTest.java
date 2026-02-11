package me.joohyuk.datahub.application.service;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import me.joohyuk.commonsaga.SagaStatus;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import me.joohyuk.datahub.fake.FakeTransformDocumentMessagePublisher;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import me.joohyuk.datahub.fake.InMemoryTransformDocumentOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TransformDocumentOutboxScheduler Classical/Detroit School 테스트
 *
 * <p>Testing Strategy: State-Based Testing (두 번째 선택)
 * <ul>
 *   <li>Output-based가 불가능한 이유: processOutboxMessage()는 void 반환</li>
 *   <li>Repository와 Outbox 엔티티의 상태 변화를 검증</li>
 *   <li>FakeMessagePublisher의 발행 기록(상태)을 검증</li>
 * </ul>
 *
 * <p>Classical School 원칙:
 * <ul>
 *   <li>실제 객체 그래프 사용: Scheduler, Handler, ObjectMapper</li>
 *   <li>Fake 구현 사용: Repository(in-memory), MessagePublisher(fake), IdGenerator(sequential)</li>
 *   <li>외부 경계에 대해서만 Fake 사용: MessagePublisher는 Kafka라는 외부 인프라 경계</li>
 *   <li>상태 기반 검증: Repository 상태, Outbox 엔티티 상태, 발행 기록 검증</li>
 *   <li>Mock 프레임워크 미사용: Mockito 등 사용하지 않음</li>
 * </ul>
 */
@DisplayName("TransformDocumentOutboxScheduler Classical 테스트")
class TransformDocumentOutboxSchedulerTest {

  // System Under Test
  private TransformDocumentOutboxScheduler scheduler;

  // Real collaborators
  private TransformDocumentOutboxHandler outboxHandler;
  private ObjectMapper objectMapper;

  // Fake implementations (for external boundaries and repositories)
  private InMemoryTransformDocumentOutboxRepository outboxRepository;
  private FakeTransformDocumentMessagePublisher messagePublisher;
  private InMemoryIdGenerator idGenerator;

  @BeforeEach
  void setUp() {
    // Given: Initialize fake repositories and external boundaries
    outboxRepository = new InMemoryTransformDocumentOutboxRepository();
    messagePublisher = new FakeTransformDocumentMessagePublisher();
    idGenerator = new InMemoryIdGenerator(1000L);

    // Given: Real ObjectMapper (Jackson - no need to mock)
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    // Given: Wire real collaborators (Classical approach - real object graph)
    outboxHandler = new TransformDocumentOutboxHandler(
        outboxRepository,
        idGenerator,
        objectMapper
    );

    // Given: System under test
    scheduler = new TransformDocumentOutboxScheduler(
        outboxHandler,
        messagePublisher
    );
  }

  @Test
  @DisplayName("PENDING 상태이고 STARTED 상태인 Outbox 메시지 2개를 처리하여 SENT 상태로 변경한다")
  void should_process_pending_started_outbox_messages_and_update_to_sent_status() {
    // Given: Two PENDING outbox messages with STARTED saga status
    TransformDocumentOutbox outbox1 = createAndSaveOutbox(
        1L,
        100L,
        "payload-1",
        OutboxStatus.PENDING,
        SagaStatus.STARTED,
        DocumentStatus.TRANSFORM_REQUESTED
    );
    TransformDocumentOutbox outbox2 = createAndSaveOutbox(
        2L,
        200L,
        "payload-2",
        OutboxStatus.PENDING,
        SagaStatus.STARTED,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher published 2 messages (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(2);

    List<FakeTransformDocumentMessagePublisher.PublishedMessage> publishedMessages =
        messagePublisher.getPublishedMessages();
    assertThat(publishedMessages)
        .extracting(FakeTransformDocumentMessagePublisher.PublishedMessage::sagaId)
        .containsExactlyInAnyOrder(100L, 200L);

    // Then: Both outbox messages updated to SENT status (state-based verification)
    TransformDocumentOutbox savedOutbox1 = outboxRepository.findById(outbox1.getId());
    TransformDocumentOutbox savedOutbox2 = outboxRepository.findById(outbox2.getId());

    assertThat(savedOutbox1.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(savedOutbox2.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);

    // Then: Other fields remain unchanged
    assertThat(savedOutbox1.getSagaStatus()).isEqualTo(SagaStatus.STARTED);
    assertThat(savedOutbox2.getSagaStatus()).isEqualTo(SagaStatus.STARTED);
  }

  @Test
  @DisplayName("처리할 PENDING 메시지가 없으면 MessagePublisher가 호출되지 않는다")
  void should_not_publish_messages_when_no_pending_messages_exist() {
    // Given: No PENDING messages exist (only SENT messages)
    createAndSaveOutbox(
        1L,
        100L,
        "payload-1",
        OutboxStatus.SENT,
        SagaStatus.STARTED,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher was not called (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isZero();
  }

  @Test
  @DisplayName("PENDING이고 COMPENSATING 상태인 Outbox 메시지를 처리한다")
  void should_process_pending_compensating_outbox_messages() {
    // Given: PENDING outbox messages with COMPENSATING saga status
    TransformDocumentOutbox outbox = createAndSaveOutbox(
        1L,
        100L,
        "compensating-payload",
        OutboxStatus.PENDING,
        SagaStatus.COMPENSATING,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher published the message (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(1);

    List<FakeTransformDocumentMessagePublisher.PublishedMessage> publishedMessages =
        messagePublisher.getPublishedMessages();
    assertThat(publishedMessages)
        .hasSize(1)
        .first()
        .satisfies(msg -> {
          assertThat(msg.sagaId()).isEqualTo(100L);
          assertThat(msg.sagaStatus()).isEqualTo(SagaStatus.COMPENSATING);
        });

    // Then: Outbox message updated to SENT status (state-based verification)
    TransformDocumentOutbox savedOutbox = outboxRepository.findById(outbox.getId());
    assertThat(savedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(savedOutbox.getSagaStatus()).isEqualTo(SagaStatus.COMPENSATING);
  }

  @Test
  @DisplayName("STARTED와 COMPENSATING 상태가 혼합된 PENDING 메시지들을 모두 처리한다")
  void should_process_all_pending_messages_with_mixed_saga_statuses() {
    // Given: Mixed saga status outbox messages
    createAndSaveOutbox(1L, 100L, "payload-started-1", OutboxStatus.PENDING,
        SagaStatus.STARTED, DocumentStatus.TRANSFORM_REQUESTED);
    createAndSaveOutbox(2L, 200L, "payload-compensating", OutboxStatus.PENDING,
        SagaStatus.COMPENSATING, DocumentStatus.TRANSFORM_REQUESTED);
    createAndSaveOutbox(3L, 300L, "payload-started-2", OutboxStatus.PENDING,
        SagaStatus.STARTED, DocumentStatus.TRANSFORM_REQUESTED);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: All 3 messages were published (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(3);

    List<FakeTransformDocumentMessagePublisher.PublishedMessage> publishedMessages =
        messagePublisher.getPublishedMessages();
    assertThat(publishedMessages)
        .extracting(FakeTransformDocumentMessagePublisher.PublishedMessage::sagaId)
        .containsExactlyInAnyOrder(100L, 200L, 300L);

    // Then: All outbox messages updated to SENT status (state-based verification)
    List<TransformDocumentOutbox> allOutboxes = outboxRepository.findAll();
    assertThat(allOutboxes)
        .hasSize(3)
        .allMatch(outbox -> outbox.getOutboxStatus() == OutboxStatus.SENT);
  }

  @Test
  @DisplayName("PENDING 상태가 아니거나 STARTED/COMPENSATING이 아닌 메시지는 처리하지 않는다")
  void should_not_process_messages_with_incorrect_status_combination() {
    // Given: Messages with wrong status combinations
    createAndSaveOutbox(1L, 100L, "payload-1", OutboxStatus.SENT,
        SagaStatus.STARTED, DocumentStatus.TRANSFORM_REQUESTED);
    createAndSaveOutbox(2L, 200L, "payload-2", OutboxStatus.PENDING,
        SagaStatus.SUCCEEDED, DocumentStatus.TRANSFORM_REQUESTED);
    createAndSaveOutbox(3L, 300L, "payload-3", OutboxStatus.FAILED,
        SagaStatus.STARTED, DocumentStatus.TRANSFORM_REQUESTED);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: No messages were published (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isZero();

    // Then: All outbox statuses remain unchanged (state-based verification)
    assertThat(outboxRepository.findById(1L).getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(outboxRepository.findById(2L).getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
    assertThat(outboxRepository.findById(3L).getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
  }

  @Test
  @DisplayName("빈 리스트를 반환받으면 어떤 처리도 하지 않는다")
  void should_do_nothing_when_repository_returns_empty_list() {
    // Given: Repository is empty (no outbox messages)
    assertThat(outboxRepository.size()).isZero();

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: No messages were published (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isZero();

    // Then: Repository remains empty (state-based verification)
    assertThat(outboxRepository.size()).isZero();
  }

  @Test
  @DisplayName("메시지 발행 실패 시 Outbox 상태를 FAILED로 업데이트한다")
  void should_update_outbox_status_to_failed_when_publish_fails() {
    // Given: PENDING outbox message exists
    TransformDocumentOutbox outbox = createAndSaveOutbox(
        1L,
        100L,
        "payload-1",
        OutboxStatus.PENDING,
        SagaStatus.STARTED,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // Given: MessagePublisher is configured to fail
    messagePublisher.setShouldFail(true);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher attempted to publish (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(1);

    // Then: Outbox status updated to FAILED (state-based verification)
    TransformDocumentOutbox savedOutbox = outboxRepository.findById(outbox.getId());
    assertThat(savedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);

    // Then: Saga status remains unchanged
    assertThat(savedOutbox.getSagaStatus()).isEqualTo(SagaStatus.STARTED);
  }

  @Test
  @DisplayName("대량의 PENDING 메시지를 순차적으로 처리한다")
  void should_process_large_batch_of_pending_messages() {
    // Given: 10 PENDING outbox messages
    for (int i = 1; i <= 10; i++) {
      createAndSaveOutbox(
          (long) i,
          (long) (i * 100),
          "payload-" + i,
          OutboxStatus.PENDING,
          i % 2 == 0 ? SagaStatus.STARTED : SagaStatus.COMPENSATING,
          DocumentStatus.TRANSFORM_REQUESTED
      );
    }

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: All 10 messages were published (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(10);

    // Then: All outbox messages updated to SENT status (state-based verification)
    List<TransformDocumentOutbox> allOutboxes = outboxRepository.findAll();
    assertThat(allOutboxes)
        .hasSize(10)
        .allMatch(outbox -> outbox.getOutboxStatus() == OutboxStatus.SENT);
  }

  @Test
  @DisplayName("메시지 처리는 각 Outbox의 불변 필드들을 보존한다")
  void should_preserve_immutable_fields_during_processing() {
    // Given: PENDING outbox message with specific immutable fields
    Long originalId = 1L;
    Long originalSagaId = 999L;
    String originalType = DOCUMENT_TRANSFORM_SAGA_NAME;
    String originalPayload = "{\"test\":\"payload\"}";
    SagaStatus originalSagaStatus = SagaStatus.STARTED;
    DocumentStatus originalDocumentStatus = DocumentStatus.TRANSFORM_REQUESTED;
    int originalVersion = 0;
    LocalDateTime originalCreatedAt = LocalDateTime.now().minusHours(1);

    TransformDocumentOutbox outbox = TransformDocumentOutbox.builder()
        .id(originalId)
        .sagaId(originalSagaId)
        .type(originalType)
        .payload(originalPayload)
        .sagaStatus(originalSagaStatus)
        .outboxStatus(OutboxStatus.PENDING)
        .documentStatus(originalDocumentStatus)
        .version(originalVersion)
        .createdAt(originalCreatedAt)
        .processedAt(null)
        .build();

    outboxRepository.save(outbox);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: Only outboxStatus changed, all other fields preserved (state-based verification)
    TransformDocumentOutbox savedOutbox = outboxRepository.findById(originalId);

    assertThat(savedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);  // Changed

    // Immutable fields preserved
    assertThat(savedOutbox.getId()).isEqualTo(originalId);
    assertThat(savedOutbox.getSagaId()).isEqualTo(originalSagaId);
    assertThat(savedOutbox.getType()).isEqualTo(originalType);
    assertThat(savedOutbox.getPayload()).isEqualTo(originalPayload);
    assertThat(savedOutbox.getSagaStatus()).isEqualTo(originalSagaStatus);
    assertThat(savedOutbox.getDocumentStatus()).isEqualTo(originalDocumentStatus);
    assertThat(savedOutbox.getVersion()).isEqualTo(originalVersion);
    assertThat(savedOutbox.getCreatedAt()).isEqualTo(originalCreatedAt);
  }

  // ─── Test Helpers (Builders) ──────────────────────────────────────

  /**
   * TransformDocumentOutbox 생성 및 저장 헬퍼
   */
  private TransformDocumentOutbox createAndSaveOutbox(
      Long id,
      Long sagaId,
      String payload,
      OutboxStatus outboxStatus,
      SagaStatus sagaStatus,
      DocumentStatus documentStatus
  ) {
    TransformDocumentOutbox outbox = TransformDocumentOutbox.builder()
        .id(id)
        .sagaId(sagaId)
        .type(DOCUMENT_TRANSFORM_SAGA_NAME)
        .payload(payload)
        .documentStatus(documentStatus)
        .sagaStatus(sagaStatus)
        .outboxStatus(outboxStatus)
        .version(0)
        .createdAt(LocalDateTime.now())
        .processedAt(null)
        .build();

    return outboxRepository.save(outbox);
  }
}
