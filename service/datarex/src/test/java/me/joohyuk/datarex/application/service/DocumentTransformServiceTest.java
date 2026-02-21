package me.joohyuk.datarex.application.service;

import static me.joohyuk.datarex.application.service.DocumentTransformRequestBuilder.aRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;
import me.joohyuk.datarex.application.service.handler.TransformDocumentHandler;
import me.joohyuk.datarex.application.service.handler.TransformDocumentResultOutboxHandler;
import me.joohyuk.datarex.domain.vo.DocumentContent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.joohyuk.datarex.fake.FakeChunkedDocumentWriter;
import me.joohyuk.datarex.fake.FakeDateTimeHolder;
import me.joohyuk.datarex.fake.FakeDocumentReader;
import me.joohyuk.datarex.fake.FakeDocumentTransformer;
import me.joohyuk.datarex.fake.FakeIdGenerator;
import me.joohyuk.datarex.fake.FakeTransformDocumentResultOutboxRepository;
import me.joohyuk.messaging.events.TransformDocumentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("문서 변환 서비스 구현체 테스트")
class DocumentTransformServiceTest {

  // Fake dependencies - these behave like real implementations but in-memory
  private FakeDocumentReader documentReader;
  private FakeDocumentTransformer documentTransformer;
  private FakeChunkedDocumentWriter chunkedDocumentWriter;
  private FakeDateTimeHolder dateTimeHolder;
  private FakeIdGenerator idGenerator;
  private FakeTransformDocumentResultOutboxRepository outboxRepository;
  private ObjectMapper objectMapper;

  // System under test - using real implementation with fake dependencies
  private TransformDocumentService transformService;

  @BeforeEach
  void setUp() {
    // Create fresh fakes for each test to ensure test isolation
    documentReader = new FakeDocumentReader();
    documentTransformer = new FakeDocumentTransformer();
    chunkedDocumentWriter = new FakeChunkedDocumentWriter();
    dateTimeHolder = new FakeDateTimeHolder();
    idGenerator = new FakeIdGenerator();
    outboxRepository = new FakeTransformDocumentResultOutboxRepository();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Create handlers with fake dependencies
    TransformDocumentHandler transformDocumentHandler = new TransformDocumentHandler(
        documentReader,
        documentTransformer,
        chunkedDocumentWriter,
        dateTimeHolder
    );

    TransformDocumentResultOutboxHandler transformDocumentResultOutboxHandler =
        new TransformDocumentResultOutboxHandler(
            idGenerator,
            dateTimeHolder,
            outboxRepository,
            objectMapper
        );

    // Wire together real service with handlers
    transformService = new TransformDocumentService(
        transformDocumentHandler,
        transformDocumentResultOutboxHandler
    );
  }

  /**
   * Helper method to extract the last event from outbox repository
   */
  private TransformDocumentCompletedEvent extractEventFromOutbox() {
    var outboxList = outboxRepository.getSaveHistory();
    if (outboxList.isEmpty()) {
      return null;
    }
    var lastOutbox = outboxList.getLast();
    try {
      return objectMapper.readValue(lastOutbox.payload(), TransformDocumentCompletedEvent.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize event from outbox", e);
    }
  }

  /**
   * Helper method to extract all events from outbox repository
   */
  private List<TransformDocumentCompletedEvent> extractAllEventsFromOutbox() {
    return outboxRepository.getSaveHistory().stream()
        .map(outbox -> {
          try {
            return objectMapper.readValue(outbox.payload(), TransformDocumentCompletedEvent.class);
          } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event from outbox", e);
          }
        })
        .toList();
  }

  @Nested
  @DisplayName("성공적인 변환 시나리오")
  class SuccessfulTransformation {

    @Test
    @DisplayName("변환 성공 시 문서를 변환하고 청크를 저장해야 한다")
    void should_transform_document_and_store_chunks_when_transformation_succeeds() {
      // Given: A document with readable content
      TransformDocumentCommand request = aRequest()
          .withDocumentId(1L)
          .withCollectionId(100L)
          .withContentHash("test-hash-123")
          .withMetadata("document.pdf", 1024L, "application/pdf")
          .build();

      List<DocumentContent> sourceDocuments = List.of(
          new DocumentContent("This is the original document content that will be chunked.")
      );
      documentReader.givenDocuments(1L, sourceDocuments);

      // The transformer will use default behavior: split into 2 chunks
      // (See FakeDocumentTransformer.createDefaultChunks)

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: Chunks should be stored
      List<DocumentContent> storedChunks = chunkedDocumentWriter.getWrittenChunks(100L, 1L);
      assertThat(storedChunks)
          .as("Chunks should be stored in the writer")
          .isNotNull()
          .hasSize(2)  // Default behavior splits into 2 chunks
          .allSatisfy(chunk -> assertThat(chunk.content()).isNotEmpty());

      // And: Storage should be called exactly once
      assertThat(chunkedDocumentWriter.getWriteCallCount())
          .as("Writer should be called once")
          .isEqualTo(1);
    }

    @Test
    @DisplayName("변환 완료 시 올바른 패시지 개수와 함께 성공 이벤트를 발행해야 한다")
    void should_publish_success_event_with_correct_passage_count_when_transformation_completes() {
      // Given: A transformable document
      TransformDocumentCommand request = aRequest()
          .withDocumentId(2L)
          .withCollectionId(200L)
          .withContentHash("content-hash-abc")
          .build();

      List<DocumentContent> sourceDocuments = List.of(
          new DocumentContent("Source document content")
      );
      documentReader.givenDocuments(2L, sourceDocuments);

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: Success event should be saved to outbox
      assertThat(outboxRepository.count())
          .as("Success event should be saved to outbox")
          .isEqualTo(1);

      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.documentId())
          .as("Event should reference the correct document")
          .isEqualTo("2");
      assertThat(event.collectionId())
          .as("Event should reference the correct collection")
          .isEqualTo("200");
      assertThat(event.contentHash())
          .as("Event should contain the content hash as passage version")
          .isEqualTo("content-hash-abc");
      assertThat(event.passageCount())
          .as("Event should report the number of chunks created")
          .isEqualTo(2);  // Default chunking creates 2 chunks
      assertThat(event.eventId())
          .as("Event should have a unique ID")
          .isNotNull();
      assertThat(event.occurredAt())
          .as("Event should have a timestamp")
          .isNotNull();

      // And: Event should be successful (no error)
      assertThat(event.isSuccess())
          .as("Event should be successful")
          .isTrue();
    }

    @Test
    @DisplayName("여러 원본 문서를 처리하고 여러 청크를 생성해야 한다")
    void should_handle_multiple_source_documents_and_create_multiple_chunks() {
      // Given: A request for a document with multiple pages/sections
      TransformDocumentCommand request = aRequest()
          .withDocumentId(3L)
          .withCollectionId(300L)
          .withContentHash("multi-doc-hash")
          .build();

      List<DocumentContent> sourceDocuments = List.of(
          new DocumentContent("First page content"),
          new DocumentContent("Second page content"),
          new DocumentContent("Third page content")
      );
      documentReader.givenDocuments(3L, sourceDocuments);

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: All pages should be chunked
      // Default behavior: each source document creates 2 chunks = 6 total
      List<DocumentContent> storedChunks = chunkedDocumentWriter.getWrittenChunks(300L, 3L);
      assertThat(storedChunks)
          .as("Should create chunks from all source documents")
          .hasSize(6);

      // And: Success event should reflect total chunk count
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.passageCount())
          .as("Event should report total chunks from all source documents")
          .isEqualTo(6);
    }

    @Test
    @DisplayName("올바른 구성 메타데이터와 함께 청크를 저장해야 한다")
    void should_store_chunks_with_correct_configuration_metadata() {
      // Given: A document transformation request
      TransformDocumentCommand request = aRequest()
          .withDocumentId(4L)
          .withCollectionId(400L)
          .withMetadata("important-doc.pdf", 2048L, "application/pdf")
          .build();

      documentReader.givenDocuments(4L, List.of(
          new DocumentContent("Document content to be chunked")
      ));

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: Storage config should match request metadata
      var storageConfig = chunkedDocumentWriter.getStorageConfig(400L, 4L);
      assertThat(storageConfig)
          .as("Storage config should be created")
          .isNotNull();
      assertThat(storageConfig.collectionId().getValue())
          .as("Collection ID should match request")
          .isEqualTo(400L);
      assertThat(storageConfig.documentId().getValue())
          .as("Document ID should match request")
          .isEqualTo(4L);
      assertThat(storageConfig.fileName())
          .as("File name should be preserved from metadata")
          .isEqualTo("important-doc.pdf");
    }
  }

  @Nested
  @DisplayName("다양한 오류 유형의 실패 시나리오")
  class FailureScenarios {

    @Test
    @DisplayName("런타임 예외로 읽기 실패 시 실패 이벤트를 발행해야 한다")
    void should_publish_failure_event_when_reading_fails_with_runtime_exception() {
      // Given: A document that will fail to read
      // Note: Since DocumentReader interface doesn't declare checked exceptions,
      // real adapters throw RuntimeException. We simulate a storage error this way.
      TransformDocumentCommand request = aRequest()
          .withDocumentId(5L)
          .withCollectionId(500L)
          .withContentHash("failing-hash")
          .build();

      RuntimeException storageError = new RuntimeException("S3 connection timeout");
      documentReader.givenReadFailure(5L, storageError);

      // When: Transformation is attempted (should not throw, handles internally)
      transformService.transformDocument(request);

      // Then: Failure event should be saved to outbox
      assertThat(outboxRepository.count())
          .as("Failure event should be saved to outbox")
          .isEqualTo(1);

      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Event should be marked as failure")
          .isFalse();
      assertThat(event.documentId())
          .as("Event should reference the correct document")
          .isEqualTo("5");
      assertThat(event.errorCode())
          .as("RuntimeException should map to UNKNOWN_ERROR")
          .isEqualTo("UNKNOWN_ERROR");
      assertThat(event.errorMessage())
          .as("Error message should contain details")
          .contains("S3 connection timeout");

      // And: No chunks should be stored
      assertThat(chunkedDocumentWriter.hasWrittenChunks(500L, 5L))
          .as("Chunks should not be stored when reading fails")
          .isFalse();
    }

    @Test
    @DisplayName("IllegalArgumentException 발생 시 UNKNOWN_ERROR 코드로 실패 이벤트를 발행해야 한다")
    void should_publish_failure_event_with_unknown_error_when_illegal_argument_exception_occurs() {
      // Given: A document that will cause validation error
      TransformDocumentCommand request = aRequest()
          .withDocumentId(6L)
          .withCollectionId(600L)
          .withContentHash("invalid-hash")
          .build();

      documentReader.givenDocuments(6L, List.of(
          new DocumentContent("Valid content")
      ));

      IllegalArgumentException validationError = new IllegalArgumentException(
          "Document content is invalid or corrupt"
      );
      documentTransformer.givenTransformFailure(validationError);

      // When: Transformation is attempted
      transformService.transformDocument(request);

      // Then: Failure event should indicate non-retryable error
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Event should be marked as failure")
          .isFalse();
      assertThat(event.errorCode())
          .as("IllegalArgumentException should map to UNKNOWN_ERROR (generic exception handler)")
          .isEqualTo("UNKNOWN_ERROR");
      assertThat(event.errorMessage())
          .as("Error message should contain validation details")
          .contains("invalid or corrupt");
    }

    @Test
    @DisplayName("런타임 예외로 쓰기 실패 시 실패 이벤트를 발행해야 한다")
    void should_publish_failure_event_when_writing_fails_with_runtime_exception() {
      // Given: Document reading and transformation succeed, but writing fails
      TransformDocumentCommand request = aRequest()
          .withDocumentId(7L)
          .withCollectionId(700L)
          .withContentHash("write-fail-hash")
          .build();

      documentReader.givenDocuments(7L, List.of(
          new DocumentContent("Content that will be transformed successfully")
      ));

      // Simulate a storage error during write
      RuntimeException writeError = new RuntimeException("Disk full - cannot write chunks");
      chunkedDocumentWriter.givenWriteFailure(writeError);

      // When: Transformation is attempted
      transformService.transformDocument(request);

      // Then: Failure event should be published
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Event should be marked as failure")
          .isFalse();
      assertThat(event.errorCode())
          .as("RuntimeException during write should map to UNKNOWN_ERROR")
          .isEqualTo("UNKNOWN_ERROR");
      assertThat(event.errorMessage())
          .as("Error message should describe the write failure")
          .contains("Disk full");
    }

    @Test
    @DisplayName("예상치 못한 예외 유형에 대해 UNKNOWN_ERROR 코드로 실패 이벤트를 발행해야 한다")
    void should_publish_failure_event_with_unknown_error_for_unexpected_exception_types() {
      // Given: An unexpected exception type
      TransformDocumentCommand request = aRequest()
          .withDocumentId(8L)
          .withCollectionId(800L)
          .withContentHash("unknown-error-hash")
          .build();

      documentReader.givenDocuments(8L, List.of(
          new DocumentContent("Content")
      ));

      // Simulate an unexpected exception type
      RuntimeException unexpectedError = new RuntimeException("Unexpected system error");
      documentTransformer.givenTransformFailure(unexpectedError);

      // When: Transformation is attempted
      transformService.transformDocument(request);

      // Then: Failure event should use UNKNOWN_ERROR code
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Event should be marked as failure")
          .isFalse();
      assertThat(event.errorCode())
          .as("Unexpected exceptions should map to UNKNOWN_ERROR")
          .isEqualTo("UNKNOWN_ERROR");
    }

    @Test
    @DisplayName("쓰기 전에 변환이 실패하면 청크를 저장하지 않아야 한다")
    void should_not_store_chunks_when_transformation_fails_before_writing() {
      // Given: A document that will fail during transformation
      TransformDocumentCommand request = aRequest()
          .withDocumentId(9L)
          .withCollectionId(900L)
          .build();

      documentReader.givenDocuments(9L, List.of(
          new DocumentContent("Content")
      ));

      documentTransformer.givenTransformFailure(
          new IllegalArgumentException("Transformation failed")
      );

      // When: Transformation is attempted
      transformService.transformDocument(request);

      // Then: No chunks should be written
      assertThat(chunkedDocumentWriter.getWriteCallCount())
          .as("Writer should not be called when transformation fails")
          .isEqualTo(0);
      assertThat(chunkedDocumentWriter.hasWrittenChunks(900L, 9L))
          .as("No chunks should exist for failed transformation")
          .isFalse();
    }

    @Test
    @DisplayName("실패 이벤트에 컨텐츠 해시와 일치하는 패시지 버전을 포함해야 한다")
    void should_include_passage_version_in_failure_event_matching_content_hash() {
      // Given: A failing transformation
      TransformDocumentCommand request = aRequest()
          .withDocumentId(10L)
          .withCollectionId(1000L)
          .withContentHash("specific-version-hash")
          .build();

      documentReader.givenReadFailure(10L, new RuntimeException("Read failed"));

      // When: Transformation fails
      transformService.transformDocument(request);

      // Then: Failure event should include the content hash
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Event should be marked as failure")
          .isFalse();
      assertThat(event.contentHash())
          .as("Failure event should include content hash (passage version)")
          .isEqualTo("specific-version-hash");
    }
  }

  @Nested
  @DisplayName("엣지 케이스 및 경계 조건")
  class EdgeCases {

    @Test
    @DisplayName("빈 문서 컨텐츠를 적절히 처리해야 한다")
    void should_handle_empty_document_content_gracefully() {
      // Given: A document with empty content
      TransformDocumentCommand request = aRequest()
          .withDocumentId(11L)
          .withCollectionId(1100L)
          .build();

      documentReader.givenDocuments(11L, List.of(
          new DocumentContent("")
      ));

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: Should still process and store (even if chunks are minimal)
      assertThat(chunkedDocumentWriter.hasWrittenChunks(1100L, 11L))
          .as("Empty content should still be processed")
          .isTrue();

      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.isSuccess())
          .as("Success event should be saved")
          .isTrue();
    }

    @Test
    @DisplayName("많은 수의 청크를 성공적으로 처리해야 한다")
    void should_handle_large_number_of_chunks_successfully() {
      // Given: A document that will produce many chunks
      TransformDocumentCommand request = aRequest()
          .withDocumentId(12L)
          .withCollectionId(1200L)
          .build();

      // Create 50 source documents (will produce 100 chunks with default behavior)
      List<DocumentContent> manyDocuments = new java.util.ArrayList<>();
      for (int i = 0; i < 50; i++) {
        manyDocuments.add(new DocumentContent("Document section " + i + " with content"));
      }
      documentReader.givenDocuments(12L, manyDocuments);

      // When: Transform the document
      transformService.transformDocument(request);

      // Then: All chunks should be stored
      List<DocumentContent> chunks = chunkedDocumentWriter.getWrittenChunks(1200L, 12L);
      assertThat(chunks)
          .as("All chunks should be stored")
          .hasSize(100);

      // And: Event should report correct count
      TransformDocumentCompletedEvent event = extractEventFromOutbox();
      assertThat(event.passageCount())
          .as("Event should report all chunks")
          .isEqualTo(100);
    }

    @Test
    @DisplayName("각 변환마다 고유한 이벤트 ID를 생성해야 한다")
    void should_generate_unique_event_ids_for_each_transformation() {
      // Given: Multiple transformation requests
      TransformDocumentCommand request1 = aRequest()
          .withDocumentId(13L)
          .withCollectionId(1300L)
          .build();
      TransformDocumentCommand request2 = aRequest()
          .withDocumentId(14L)
          .withCollectionId(1400L)
          .build();

      documentReader.givenDocuments(13L, List.of(new DocumentContent("Content 1")));
      documentReader.givenDocuments(14L, List.of(new DocumentContent("Content 2")));

      // When: Transform both documents
      transformService.transformDocument(request1);
      transformService.transformDocument(request2);

      // Then: Each should have a unique event ID
      List<TransformDocumentCompletedEvent> events = extractAllEventsFromOutbox();
      assertThat(events)
          .hasSize(2)
          .extracting(TransformDocumentCompletedEvent::eventId)
          .doesNotHaveDuplicates();
    }
  }
}
