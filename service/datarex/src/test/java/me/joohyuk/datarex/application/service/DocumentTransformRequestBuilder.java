package me.joohyuk.datarex.application.service;

import com.spartaecommerce.domain.vo.Metadata;
import com.spartaecommerce.domain.vo.TrackingId;
import java.util.UUID;
import me.joohyuk.datarex.application.dto.command.TransformDocumentCommand;

/**
 * Builder for creating TransformDocumentCommand test fixtures.
 * <p>
 * Provides a fluent API for constructing test commands with sensible defaults and specific
 * overrides.
 */
public class DocumentTransformRequestBuilder {

  private TrackingId trackingId = new TrackingId(UUID.randomUUID());
  // Generate unique correlationId for each builder instance
  private String correlationId = UUID.randomUUID().toString();
  private Long documentId = 1L;
  private Long collectionId = 100L;
  private String fileKey = "collections/100/documents/1/file.pdf";
  // Valid 64-char SHA-256 hex string
  private String contentHash = "a".repeat(64);
  private Metadata metadata = Metadata.forUpload(
      "test-document.pdf",
      1024L,
      "application/pdf",
      999L
  );
  private int attempt = 0;

  private DocumentTransformRequestBuilder() {
  }

  public static DocumentTransformRequestBuilder aRequest() {
    return new DocumentTransformRequestBuilder();
  }

  public DocumentTransformRequestBuilder withTrackingId(TrackingId trackingId) {
    this.trackingId = trackingId;
    return this;
  }

  public DocumentTransformRequestBuilder withCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
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

  public DocumentTransformRequestBuilder withAttempt(int attempt) {
    this.attempt = attempt;
    return this;
  }

  public TransformDocumentCommand build() {
    return new TransformDocumentCommand(
        trackingId,
        correlationId,
        documentId,
        collectionId,
        fileKey,
        contentHash,
        metadata,
        attempt
    );
  }

  /**
   * Creates a command for a markdown document.
   */
  public static TransformDocumentCommand markdownDocument() {
    return aRequest()
        .withMetadata("README.md", 2048L, "text/markdown")
        .withFileKey("collections/100/documents/1/README.md")
        .build();
  }

  /**
   * Creates a command for a PDF document.
   */
  public static TransformDocumentCommand pdfDocument() {
    return aRequest()
        .withMetadata("report.pdf", 10240L, "application/pdf")
        .withFileKey("collections/100/documents/1/report.pdf")
        .build();
  }

  /**
   * Creates a command for a large document.
   */
  public static TransformDocumentCommand largeDocument() {
    return aRequest()
        .withMetadata("large-doc.txt", 1048576L, "text/plain")
        .withFileKey("collections/100/documents/1/large-doc.txt")
        .build();
  }
}
