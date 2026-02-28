package me.joohyuk.datahub.application.service;

import static me.joohyuk.commonsaga.SagaConstants.DOCUMENT_TRANSFORM_SAGA_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.JsonSerializer;
import com.spartaecommerce.infrastructure.json.ObjectMapperJsonSerializer;
import com.spartaecommerce.outbox.OutboxStatus;
import com.spartaecommerce.util.DateTimeHolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import me.joohyuk.datahub.fake.FakeDateTimeHolder;
import me.joohyuk.datahub.fake.FakeTransformDocumentMessagePublisher;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import me.joohyuk.datahub.fake.InMemoryTransformDocumentOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TransformDocumentOutboxScheduler Classical/Detroit School 테스트
 *
 * <p>Testing Strategy: State-Based Testing
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
  private DateTimeHolder dateTimeHolder;

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
    dateTimeHolder = new FakeDateTimeHolder(java.time.Instant.parse("2024-01-01T10:00:00Z"));

    // Given: Real ObjectMapper (Jackson - no need to mock)
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    // Given: Real JsonSerializer with ObjectMapper
    JsonSerializer jsonSerializer = new ObjectMapperJsonSerializer(objectMapper);

    // Given: Wire real collaborators (Classical approach - real object graph)
    outboxHandler = new TransformDocumentOutboxHandler(
        outboxRepository,
        idGenerator,
        jsonSerializer,
        dateTimeHolder
    );

    // Given: System under test
    scheduler = new TransformDocumentOutboxScheduler(
        outboxHandler,
        messagePublisher
    );
  }

  @Test
  @DisplayName("PENDING 상태의 Outbox 메시지 2개를 처리하여 SENT 상태로 변경한다")
  void should_process_pending_outbox_messages_and_update_to_sent_status() {
    // Given: Two PENDING outbox messages
    TransformDocumentOutbox outbox1 = createAndSaveOutbox(
        1L,
        "correlation-1",
        "payload-1",
        OutboxStatus.PENDING,
        DocumentStatus.TRANSFORM_REQUESTED
    );
    TransformDocumentOutbox outbox2 = createAndSaveOutbox(
        2L,
        "correlation-2",
        "payload-2",
        OutboxStatus.PENDING,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher published 2 messages (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isEqualTo(2);

    List<FakeTransformDocumentMessagePublisher.PublishedMessage> publishedMessages =
        messagePublisher.getPublishedMessages();
    assertThat(publishedMessages)
        .extracting(FakeTransformDocumentMessagePublisher.PublishedMessage::correlationId)
        .containsExactlyInAnyOrder("correlation-1", "correlation-2");

    // Then: Both outbox messages updated to SENT status (state-based verification)
    TransformDocumentOutbox savedOutbox1 = outboxRepository.findById(outbox1.getId());
    TransformDocumentOutbox savedOutbox2 = outboxRepository.findById(outbox2.getId());

    assertThat(savedOutbox1.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(savedOutbox2.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
  }

  @Test
  @DisplayName("처리할 PENDING 메시지가 없으면 MessagePublisher가 호출되지 않는다")
  void should_not_publish_messages_when_no_pending_messages_exist() {
    // Given: No PENDING messages exist (only SENT messages)
    createAndSaveOutbox(
        1L,
        "correlation-1",
        "payload-1",
        OutboxStatus.SENT,
        DocumentStatus.TRANSFORM_REQUESTED
    );

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: MessagePublisher was not called (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isZero();
  }

  @Test
  @DisplayName("PENDING이 아닌 메시지는 처리하지 않는다")
  void should_not_process_messages_with_non_pending_status() {
    // Given: Messages with non-PENDING status
    createAndSaveOutbox(1L, "correlation-1", "payload-1", OutboxStatus.SENT,
        DocumentStatus.TRANSFORM_REQUESTED);
    createAndSaveOutbox(2L, "correlation-2", "payload-2", OutboxStatus.FAILED,
        DocumentStatus.TRANSFORM_REQUESTED);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: No messages were published (state-based verification)
    assertThat(messagePublisher.getPublishedMessageCount()).isZero();

    // Then: All outbox statuses remain unchanged (state-based verification)
    assertThat(outboxRepository.findById(1L).getOutboxStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(outboxRepository.findById(2L).getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
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
        "correlation-1",
        "payload-1",
        OutboxStatus.PENDING,
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
  }

  @Test
  @DisplayName("대량의 PENDING 메시지를 순차적으로 처리한다")
  void should_process_large_batch_of_pending_messages() {
    // Given: 10 PENDING outbox messages
    for (int i = 1; i <= 10; i++) {
      createAndSaveOutbox(
          (long) i,
          "correlation-" + i,
          "payload-" + i,
          OutboxStatus.PENDING,
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
    String originalCorrelationId = UUID.randomUUID().toString();
    String originalType = DOCUMENT_TRANSFORM_SAGA_NAME;
    String originalPayload = "{\"test\":\"payload\"}";
    DocumentStatus originalDocumentStatus = DocumentStatus.TRANSFORM_REQUESTED;
    int originalVersion = 0;
    LocalDateTime originalCreatedAt = LocalDateTime.now().minusHours(1);

    TransformDocumentOutbox outbox = TransformDocumentOutbox.builder()
        .id(originalId)
        .correlationId(originalCorrelationId)
        .type(originalType)
        .payload(originalPayload)
        .outboxStatus(OutboxStatus.PENDING)
        .documentStatus(originalDocumentStatus)
        .version(originalVersion)
        .createdAt(originalCreatedAt)
        .build();

    outboxRepository.save(outbox);

    // When: processOutboxMessage is executed
    scheduler.processOutboxMessage();

    // Then: Only outboxStatus changed, all other fields preserved (state-based verification)
    TransformDocumentOutbox savedOutbox = outboxRepository.findById(originalId);

    assertThat(savedOutbox.getOutboxStatus()).isEqualTo(OutboxStatus.SENT);  // Changed

    // Immutable fields preserved
    assertThat(savedOutbox.getId()).isEqualTo(originalId);
    assertThat(savedOutbox.getCorrelationId()).isEqualTo(originalCorrelationId);
    assertThat(savedOutbox.getType()).isEqualTo(originalType);
    assertThat(savedOutbox.getPayload()).isEqualTo(originalPayload);
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
      String correlationId,
      String payload,
      OutboxStatus outboxStatus,
      DocumentStatus documentStatus
  ) {
    TransformDocumentOutbox outbox = TransformDocumentOutbox.builder()
        .id(id)
        .correlationId(correlationId)
        .type(DOCUMENT_TRANSFORM_SAGA_NAME)
        .payload(payload)
        .documentStatus(documentStatus)
        .outboxStatus(outboxStatus)
        .version(0)
        .createdAt(LocalDateTime.now())
        .build();

    return outboxRepository.save(outbox);
  }
}
