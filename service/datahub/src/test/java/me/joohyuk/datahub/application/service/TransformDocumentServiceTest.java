package me.joohyuk.datahub.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartaecommerce.domain.port.JsonSerializer;
import com.spartaecommerce.domain.vo.CollectionId;
import com.spartaecommerce.domain.vo.ContentHash;
import com.spartaecommerce.domain.vo.DocumentId;
import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.TrackingId;
import com.spartaecommerce.infrastructure.json.ObjectMapperJsonSerializer;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import me.joohyuk.datahub.application.dto.result.TransformDocumentRequestsResult;
import me.joohyuk.datahub.application.service.handler.UploadDocumentHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentOutboxHandler;
import me.joohyuk.datahub.application.service.handler.TransformDocumentSagaHandler;
import me.joohyuk.datahub.domain.entity.Document;
import me.joohyuk.datahub.domain.entity.DocumentCollection;
import me.joohyuk.datahub.domain.entity.TransformDocumentOutbox;
import me.joohyuk.datahub.domain.exception.DatahubDomainException;
import me.joohyuk.datahub.domain.vo.DocumentStatus;
import me.joohyuk.datahub.fake.FakeDateTimeHolder;
import me.joohyuk.datahub.fake.InMemoryDocumentCollectionRepository;
import me.joohyuk.datahub.fake.InMemoryDocumentRepository;
import me.joohyuk.datahub.fake.InMemoryIdGenerator;
import me.joohyuk.datahub.fake.InMemoryTransformDocumentOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TransformDocumentService Classical 테스트")
class TransformDocumentServiceTest {

  // System Under Test
  private TransformDocumentService transformDocumentService;

  // Fake implementations (instead of mocks)
  private InMemoryDocumentRepository documentRepository;
  private InMemoryDocumentCollectionRepository documentCollectionRepository;
  private InMemoryTransformDocumentOutboxRepository transformDocumentOutboxRepository;
  private InMemoryIdGenerator documentIdGenerator;

  private Instant fixedNow;

  @BeforeEach
  void setUp() {
    // Given: Fixed timestamp for deterministic behavior
    fixedNow = Instant.parse("2024-01-01T10:00:00Z");

    // Given: Initialize fake repositories
    documentRepository = new InMemoryDocumentRepository();
    documentCollectionRepository = new InMemoryDocumentCollectionRepository();
    transformDocumentOutboxRepository = new InMemoryTransformDocumentOutboxRepository();

    // Given: Initialize fake infrastructure with separate ID generators
    FakeDateTimeHolder dateTimeHolder = new FakeDateTimeHolder(fixedNow);
    documentIdGenerator = new InMemoryIdGenerator(1000L);
    InMemoryIdGenerator outboxIdGenerator = new InMemoryIdGenerator(2000L);

    // Given: Real ObjectMapper
    // Real ObjectMapper (no need to mock)
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    // Given: Wire real collaborators (Classical approach)
    TransformDocumentSagaHandler transformDocumentSagaHandler = new TransformDocumentSagaHandler();

    UploadDocumentHandler uploadDocumentHandler = new UploadDocumentHandler(
        documentRepository,
        documentCollectionRepository,
        dateTimeHolder,
        documentIdGenerator
    );

    // Real JsonSerializer
    JsonSerializer jsonSerializer = new ObjectMapperJsonSerializer(objectMapper);

    // Real collaborators (Classical approach - no mocks)
    TransformDocumentHandler transformDocumentHandler = new TransformDocumentHandler(
        documentRepository,
        documentCollectionRepository,
        dateTimeHolder,
        documentIdGenerator
    );

    TransformDocumentOutboxHandler transformDocumentOutboxHandler = new TransformDocumentOutboxHandler(
        transformDocumentOutboxRepository,
        transformDocumentSagaHandler,
        outboxIdGenerator,
        jsonSerializer
    );

    // Given: System under test with real collaborators
    transformDocumentService = new TransformDocumentService(
        transformDocumentHandler,
        transformDocumentOutboxHandler
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 5, 10})
  @DisplayName("유효한 컬렉션에 문서들이 있을 때 transform 호출 시 TrackingId들을 반환하고 TRANSFORM_REQUESTED 상태로 문서들을 저장한다")
  void transform_with_valid_collection_containing_documents(int documentCount) {
    // Given: Valid collection exists
    CollectionId collectionId = CollectionId.of(100L);
    DocumentCollection collection = createAndSaveCollection(collectionId, "test-collection");

    // Given: Documents in UPLOADED status
    List<Document> uploadedDocuments = createAndSaveUploadedDocuments(
        collectionId,
        documentCount
    );

    // When: Transform is called
    TransformDocumentRequestsResult result = transformDocumentService.transform(collectionId);

    // Then: Result contains correct tracking IDs
    assertThat(result.transformTrackingIds())
        .hasSize(documentCount)
        .containsExactlyInAnyOrderElementsOf(
            uploadedDocuments.stream()
                .map(Document::getTrackingId)
                .toList()
        );

    // Then: Documents were persisted with TRANSFORM_REQUESTED status (state-based verification)
    List<Document> savedDocuments = documentRepository.findByCollectionId(
        collectionId,
        DocumentStatus.TRANSFORM_REQUESTED
    );
    assertThat(savedDocuments).hasSize(documentCount);
    assertAllDocumentsHaveTransformRequestedState(savedDocuments);

    // Then: Outbox entries were created (state-based verification)
    List<TransformDocumentOutbox> savedOutboxes = transformDocumentOutboxRepository.findAll();
    assertThat(savedOutboxes).hasSize(documentCount);
    assertAllOutboxesHaveCorrectTransformRequestedState(savedOutboxes);
  }

  @Test
  @DisplayName("컬렉션에 UPLOADED 상태의 문서가 없을 때 빈 결과를 반환한다")
  void return_empty_result_when_no_uploaded_documents_exist() {
    // Given: Valid collection exists but has no UPLOADED documents
    CollectionId collectionId = CollectionId.of(200L);
    createAndSaveCollection(collectionId, "empty-collection");

    // When: Transform is called
    TransformDocumentRequestsResult result = transformDocumentService.transform(collectionId);

    // Then: Empty result is returned
    assertThat(result).isNotNull();
    assertThat(result.transformTrackingIds()).isEmpty();

    // Then: No documents were persisted (state-based verification)
    List<Document> savedDocuments = documentRepository.findByCollectionId(collectionId,
        DocumentStatus.TRANSFORM_REQUESTED);
    assertThat(savedDocuments).isEmpty();

    // Then: No outbox entries were created (state-based verification)
    List<TransformDocumentOutbox> savedOutboxes = transformDocumentOutboxRepository.findAll();
    assertThat(savedOutboxes).isEmpty();
  }

  @Test
  @DisplayName("컬렉션이 존재하지 않을 때 예외를 던진다")
  void throw_exception_when_collection_not_found() {
    // Given: Non-existent collection
    CollectionId nonExistentCollectionId = CollectionId.of(999L);

    // When & Then: Transform throws exception
    assertThatThrownBy(() -> transformDocumentService.transform(nonExistentCollectionId))
        .isInstanceOf(DatahubDomainException.class)
        .hasMessageContaining("Failed to find Collection");

    // Then: No repository changes occurred (state-based verification)
    assertThat(documentRepository.findAll()).isEmpty();
    assertThat(transformDocumentOutboxRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName("transform 중 문서의 불변 필드들을 보존한다")
  void preserve_document_immutable_fields_during_transform() {
    // Given: Document with specific identity and metadata
    CollectionId collectionId = CollectionId.of(400L);
    createAndSaveCollection(collectionId, "preserve-test-collection");

    String originalFileKey = "original/path/document.md";
    ContentHash originalHash = createContentHash("original-hash-value");
    Metadata originalMetadata = createMetadata("document.md");

    Document document = createAndSaveUploadedDocumentWithDetails(
        collectionId,
        originalFileKey,
        originalHash,
        originalMetadata
    );

    TrackingId originalTrackingId = document.getTrackingId();

    // When: Transform is called
    transformDocumentService.transform(collectionId);

    // Then: Immutable fields are preserved (state-based verification)
    Document transformedDocument = documentRepository.getById(document.getId());
    assertDocumentPreservesImmutableFields(
        transformedDocument,
        collectionId,
        originalFileKey,
        originalHash,
        originalMetadata,
        originalTrackingId
    );

    // Then: Only status and timestamp changed
    assertDocumentHasTransformRequestedState(transformedDocument);
  }

  @Test
  @DisplayName("올바른 Saga 상태 매핑으로 Outbox 엔트리를 생성한다")
  void create_outbox_entries_with_correct_saga_status_mapping() {
    // Given: Documents ready for transform
    CollectionId collectionId = CollectionId.of(500L);
    createAndSaveCollection(collectionId, "saga-test-collection");

    List<Document> documents = createAndSaveUploadedDocuments(collectionId, 3);

    // When: Transform is called
    transformDocumentService.transform(collectionId);

    // Then: Outbox entries have correct saga status (state-based verification)
    List<TransformDocumentOutbox> outboxEntries = transformDocumentOutboxRepository.findAll();
    assertThat(outboxEntries).hasSize(3);
    assertAllOutboxesHaveCorrectTransformRequestedState(outboxEntries);
  }

  @Test
  @DisplayName("문서와 Outbox 엔트리 간의 참조 일관성을 유지한다")
  void maintain_referential_consistency_between_documents_and_outbox() {
    // Given: Collection with documents
    CollectionId collectionId = CollectionId.of(600L);
    createAndSaveCollection(collectionId, "consistency-test-collection");

    List<Document> documents = createAndSaveUploadedDocuments(collectionId, 2);

    // When: Transform is called
    TransformDocumentRequestsResult result = transformDocumentService.transform(collectionId);

    // Then: Same number of documents and outbox entries (state-based verification)
    List<Document> savedDocuments = documentRepository.findByCollectionId(collectionId,
        DocumentStatus.TRANSFORM_REQUESTED);
    List<TransformDocumentOutbox> savedOutboxes = transformDocumentOutboxRepository.findAll();

    assertThat(savedDocuments).hasSize(2);
    assertThat(savedOutboxes).hasSize(2);

    // Then: Result tracking IDs match saved documents
    List<TrackingId> resultTrackingIds = result.transformTrackingIds();
    List<TrackingId> documentTrackingIds = savedDocuments.stream()
        .map(Document::getTrackingId)
        .toList();

    assertThat(resultTrackingIds).containsExactlyInAnyOrderElementsOf(documentTrackingIds);
  }

  // ─── Assertion Helpers ──────────────────────────────────────

  private void assertAllDocumentsHaveTransformRequestedState(List<Document> documents) {
    assertThat(documents)
        .allSatisfy(this::assertDocumentHasTransformRequestedState);
  }

  private void assertDocumentHasTransformRequestedState(Document document) {
    assertThat(document.getStatus()).isEqualTo(DocumentStatus.TRANSFORM_REQUESTED);
    assertThat(document.getUpdatedAt()).isEqualTo(fixedNow);
  }

  private void assertAllOutboxesHaveCorrectTransformRequestedState(
      List<TransformDocumentOutbox> outboxes
  ) {
    assertThat(outboxes)
        .allSatisfy(outbox -> {
          assertThat(outbox.getType()).isEqualTo("TransformDocumentSaga");
          assertThat(outbox.getDocumentStatus()).isEqualTo(DocumentStatus.TRANSFORM_REQUESTED);
          assertThat(outbox.getSagaStatus().name()).isEqualTo("STARTED");
          assertThat(outbox.getOutboxStatus().name()).isEqualTo("PENDING");
          assertThat(outbox.getPayload()).isNotBlank();
        });
  }

  private void assertDocumentPreservesImmutableFields(
      Document document,
      CollectionId expectedCollectionId,
      String expectedFileKey,
      ContentHash expectedContentHash,
      Metadata expectedMetadata,
      TrackingId expectedTrackingId
  ) {
    assertThat(document.getCollectionId()).isEqualTo(expectedCollectionId);
    assertThat(document.getFileKey()).isEqualTo(expectedFileKey);
    assertThat(document.getContentHash()).isEqualTo(expectedContentHash);
    assertThat(document.getMetadata()).isEqualTo(expectedMetadata);
    assertThat(document.getTrackingId()).isEqualTo(expectedTrackingId);
  }

  // ─── Test Helpers (Builders) ──────────────────────────────────────

  private DocumentCollection createAndSaveCollection(CollectionId collectionId, String name) {
    DocumentCollection collection = DocumentCollection.of(
        collectionId.getValue(),
        name,
        "Test collection description",
        fixedNow,
        fixedNow
    );
    return documentCollectionRepository.save(collection);
  }

  private List<Document> createAndSaveUploadedDocuments(CollectionId collectionId, int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> createAndSaveUploadedDocument(
            collectionId,
            "document-" + i + ".pdf"
        ))
        .toList();
  }

  private Document createAndSaveUploadedDocument(CollectionId collectionId, String fileName) {
    return createAndSaveUploadedDocumentWithDetails(
        collectionId,
        "documents/" + fileName,
        createContentHash("hash-" + fileName),
        createMetadata(fileName)
    );
  }

  private Document createAndSaveUploadedDocumentWithDetails(
      CollectionId collectionId,
      String fileKey,
      ContentHash contentHash,
      Metadata metadata
  ) {
    Document document = Document.create(collectionId, fileKey, contentHash, metadata);
    DocumentId documentId = new DocumentId(documentIdGenerator.generateId());
    document.upload(documentId, fixedNow);

    return documentRepository.save(document);
  }

  private ContentHash createContentHash(String seed) {
    // Create valid 64-character SHA-256 hex string
    String hash = String.format("%064d", seed.hashCode()).replace('-', '0');
    return ContentHash.of(hash.substring(0, 64));
  }

  private Metadata createMetadata(String fileName) {
    return Metadata.forUpload(
        fileName,
        1024L,
        "application/pdf",
        100L // uploadedBy user ID
    );
  }
}
