package me.joohyuk.datarex.application.service;

import com.spartaecommerce.domain.vo.Metadata;
import java.time.Instant;
import java.time.LocalDateTime;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage;
import me.joohyuk.messaging.events.DocumentTransformRequestedMessage.DocumentTransformRequest;

/**
 * Builder for creating DocumentTransformRequestedMessage test fixtures.
 * <p>
 * Provides a fluent API for constructing test messages with sensible defaults and specific overrides.
 */
public class DocumentTransformRequestBuilder {

  private Long documentId = 1L;
  private Long collectionId = 100L;
  private String fileKey = "collections/100/documents/1/file.pdf";
  private String contentHash = "abc123hash";
  private Metadata metadata = Metadata.forUpload(
      "test-document.pdf",
      1024L,
      "application/pdf",
      999L
  );
  private String status = "PENDING";
  private int attempt = 0;
  private String lastErrorCode = null;
  private String lastErrorMessage = null;
  private int passageCount = 0;
  private String lastResultEventId = null;
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();
  private LocalDateTime messageCreatedAt = LocalDateTime.now();

  private DocumentTransformRequestBuilder() {
  }

  public static DocumentTransformRequestBuilder aRequest() {
    return new DocumentTransformRequestBuilder();
  }

  public DocumentTransformRequestBuilder withDocumentId(Long documentId) {
    this.documentId = documentId;
    return this;
  }

  public DocumentTransformRequestBuilder withCollectionId(Long collectionId) {
    this.collectionId = collectionId;
    return this;
  }

  public DocumentTransformRequestBuilder withFileKey(String fileKey) {
    this.fileKey = fileKey;
    return this;
  }

  public DocumentTransformRequestBuilder withContentHash(String contentHash) {
    this.contentHash = contentHash;
    return this;
  }

  public DocumentTransformRequestBuilder withMetadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public DocumentTransformRequestBuilder withMetadata(
      String fileName,
      Long fileSize,
      String contentType
  ) {
    this.metadata = Metadata.forUpload(fileName, fileSize, contentType, 999L);
    return this;
  }

  public DocumentTransformRequestBuilder withStatus(String status) {
    this.status = status;
    return this;
  }

  public DocumentTransformRequestBuilder withAttempt(int attempt) {
    this.attempt = attempt;
    return this;
  }

  public DocumentTransformRequestBuilder withLastError(String errorCode, String errorMessage) {
    this.lastErrorCode = errorCode;
    this.lastErrorMessage = errorMessage;
    return this;
  }

  public DocumentTransformRequestBuilder withPassageCount(int passageCount) {
    this.passageCount = passageCount;
    return this;
  }

  public DocumentTransformRequestBuilder withCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public DocumentTransformRequestedMessage build() {
    DocumentTransformRequest request = new DocumentTransformRequest(
        documentId,
        collectionId,
        fileKey,
        contentHash,
        metadata,
        status,
        attempt,
        lastErrorCode,
        lastErrorMessage,
        passageCount,
        lastResultEventId,
        createdAt,
        updatedAt
    );

    return new DocumentTransformRequestedMessage(request, messageCreatedAt);
  }

  /**
   * Creates a request for a markdown document.
   */
  public static DocumentTransformRequestedMessage markdownDocument() {
    return aRequest()
        .withMetadata("README.md", 2048L, "text/markdown")
        .withFileKey("collections/100/documents/1/README.md")
        .build();
  }

  /**
   * Creates a request for a PDF document.
   */
  public static DocumentTransformRequestedMessage pdfDocument() {
    return aRequest()
        .withMetadata("report.pdf", 10240L, "application/pdf")
        .withFileKey("collections/100/documents/1/report.pdf")
        .build();
  }

  /**
   * Creates a request for a large document.
   */
  public static DocumentTransformRequestedMessage largeDocument() {
    return aRequest()
        .withMetadata("large-doc.txt", 1048576L, "text/plain")
        .withFileKey("collections/100/documents/1/large-doc.txt")
        .build();
  }
}
